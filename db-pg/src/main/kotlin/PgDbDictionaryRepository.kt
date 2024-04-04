package com.gitlab.sszuev.flashcards.dbpg

import com.gitlab.sszuev.flashcards.common.SysConfig
import com.gitlab.sszuev.flashcards.common.asLong
import com.gitlab.sszuev.flashcards.common.documents.DocumentCard
import com.gitlab.sszuev.flashcards.common.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.common.documents.createReader
import com.gitlab.sszuev.flashcards.common.documents.createWriter
import com.gitlab.sszuev.flashcards.common.forbiddenEntityDbError
import com.gitlab.sszuev.flashcards.common.noDictionaryFoundDbError
import com.gitlab.sszuev.flashcards.common.parseCardWordsJson
import com.gitlab.sszuev.flashcards.common.status
import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.common.toDocumentExamples
import com.gitlab.sszuev.flashcards.common.toDocumentTranslations
import com.gitlab.sszuev.flashcards.common.wrongResourceDbError
import com.gitlab.sszuev.flashcards.dbpg.dao.Cards
import com.gitlab.sszuev.flashcards.dbpg.dao.Dictionaries
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbCard
import com.gitlab.sszuev.flashcards.dbpg.dao.PgDbDictionary
import com.gitlab.sszuev.flashcards.model.common.AppError
import com.gitlab.sszuev.flashcards.model.common.AppUserId
import com.gitlab.sszuev.flashcards.model.domain.DictionaryEntity
import com.gitlab.sszuev.flashcards.model.domain.DictionaryId
import com.gitlab.sszuev.flashcards.model.domain.ResourceEntity
import com.gitlab.sszuev.flashcards.repositories.DbDataException
import com.gitlab.sszuev.flashcards.repositories.DbDictionary
import com.gitlab.sszuev.flashcards.repositories.DbDictionaryRepository
import com.gitlab.sszuev.flashcards.repositories.DictionaryDbResponse
import com.gitlab.sszuev.flashcards.repositories.ImportDictionaryDbResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class PgDbDictionaryRepository(
    dbConfig: PgDbConfig = PgDbConfig(),
    private val sysConfig: SysConfig = SysConfig(),
) : DbDictionaryRepository {
    private val connection by lazy {
        // lazy, to avoid initialization error when there is no real pg-database
        // and memory-storage is used instead
        PgDbConnector.connection(dbConfig)
    }

    override fun findDictionaryById(dictionaryId: String): DbDictionary? = connection.execute {
        PgDbDictionary.findById(dictionaryId.toLong())?.toDbDictionary()
    }

    override fun findDictionariesByUserId(userId: String): Sequence<DbDictionary> = connection.execute {
        PgDbDictionary.find(Dictionaries.userId eq userId.toUserId()).map { it.toDbDictionary() }.asSequence()
    }

    override fun createDictionary(entity: DbDictionary): DbDictionary = connection.execute {
        val timestamp = systemNow()
        val dictionaryId = Dictionaries.insertAndGetId {
            it[sourceLanguage] = entity.sourceLang.langId
            it[targetLanguage] = entity.targetLang.langId
            it[name] = entity.name
            it[userId] = entity.userId.toLong()
            it[changedAt] = timestamp
        }
        entity.copy(dictionaryId = dictionaryId.value.toString())
    }

    override fun deleteDictionary(dictionaryId: String): DbDictionary = connection.execute {
        require(dictionaryId.isNotBlank())
        val id = dictionaryId.toLong()
        val found = PgDbDictionary.findById(id)?.toDbDictionary() ?: throw DbDataException("Can't find dictionary $id")
        val cardIds = Cards.selectAll().where {
            Cards.dictionaryId eq id
        }.map {
            it[Cards.id]
        }
        Cards.deleteWhere {
            this.id inList cardIds
        }
        val res = Dictionaries.deleteWhere {
            Dictionaries.id eq id
        }
        if (res != 1) {
            throw DbDataException("Can't delete dictionary $id")
        }
        found
    }

    override fun importDictionary(userId: AppUserId, dictionaryId: DictionaryId): ImportDictionaryDbResponse {
        return connection.execute {
            val errors = mutableListOf<AppError>()
            val found = checkDictionaryUser("importDictionary", userId, dictionaryId, errors)
            if (errors.isNotEmpty()) {
                return@execute ImportDictionaryDbResponse(errors = errors)
            }
            checkNotNull(found)
            val cards = PgDbCard.find {
                Cards.dictionaryId eq found.id
            }
            val res = DocumentDictionary(
                name = found.name,
                sourceLang = found.sourceLang,
                targetLang = found.targetLang,
                cards = cards.map { card ->
                    val word = parseCardWordsJson(card.words).first()
                    DocumentCard(
                        text = word.word,
                        transcription = word.transcription,
                        partOfSpeech = word.partOfSpeech,
                        translations = word.toDocumentTranslations(),
                        examples = word.toDocumentExamples(),
                        status = sysConfig.status(card.answered),
                    )
                }
            )
            val data = try {
                createWriter().write(res)
            } catch (ex: Exception) {
                return@execute ImportDictionaryDbResponse(wrongResourceDbError(ex))
            }
            ImportDictionaryDbResponse(resource = ResourceEntity(dictionaryId, data))
        }
    }

    override fun exportDictionary(userId: AppUserId, resource: ResourceEntity): DictionaryDbResponse {
        val timestamp = systemNow()
        val document = try {
            createReader().parse(resource.data)
        } catch (ex: Exception) {
            return DictionaryDbResponse(wrongResourceDbError(ex))
        }
        return connection.execute {
            val sourceLang = document.sourceLang
            val targetLang = document.targetLang
            val dictionaryId = Dictionaries.insertAndGetId {
                it[sourceLanguage] = sourceLang
                it[targetLanguage] = targetLang
                it[name] = document.name
                it[Dictionaries.userId] = userId.asLong()
                it[changedAt] = timestamp
            }
            document.cards.forEach {
                PgDbCard.new {
                    this.dictionaryId = dictionaryId
                    this.words = it.toPgDbCardWordsJson()
                    this.details = "{}"
                    this.changedAt = timestamp
                }
            }
            val res = DictionaryEntity(
                dictionaryId = dictionaryId.asDictionaryId(),
                name = document.name,
                sourceLang = createLangEntity(sourceLang),
                targetLang = createLangEntity(targetLang),
            )
            DictionaryDbResponse(dictionary = res)
        }
    }

    @Suppress("DuplicatedCode", "SameParameterValue")
    private fun checkDictionaryUser(
        operation: String,
        userId: AppUserId,
        dictionaryId: DictionaryId,
        errors: MutableList<AppError>
    ): PgDbDictionary? {
        val dictionary = PgDbDictionary.findById(dictionaryId.asLong())
        if (dictionary == null) {
            errors.add(noDictionaryFoundDbError(operation, dictionaryId))
            return null
        }
        if (dictionary.userId.value == userId.asLong()) {
            return dictionary
        }
        errors.add(forbiddenEntityDbError(operation, dictionaryId, userId))
        return null
    }
}