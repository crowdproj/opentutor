package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
import com.gitlab.sszuev.flashcards.systemNow
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * A dictionary store, attached to a file system or classpath.
 * In the first case, it is persistent.
 */
class MemDatabase private constructor(
    private val idGenerator: IdSequences,
    private val dictionaries: MutableMap<String, MutableMap<Long, DictionaryResource>>,
    private val users: MutableMap<String, MemDbUser>,
    private val databaseHomeDirectory: String?,
) {

    @Volatile
    private var dictionariesChanged = false

    @Volatile
    private var cardsChanged = false

    @Volatile
    private var usersChanged = false

    fun countUsers(): Long {
        return users.size.toLong()
    }

    fun findUserIds(): Sequence<String> {
        return users.keys.asSequence()
    }

    fun findUserById(id: String): MemDbUser? {
        return users[id]
    }

    fun saveUser(user: MemDbUser): MemDbUser {
        val userId = requireNotNull(user.id) { "No id in the user-record $user" }
        users[userId] = user
        usersChanged = true
        return user
    }

    fun countDictionaries(): Long {
        return dictionaries.asSequence().sumOf { it.value.size.toLong() }
    }

    fun findDictionariesByUserId(userId: String): Sequence<MemDbDictionary> {
        return dictionaries[userId]?.values?.asSequence()?.map { it.dictionary } ?: emptySequence()
    }

    fun findDictionariesByIds(dictionaryIds: Collection<Long>): Sequence<MemDbDictionary> {
        val ids = dictionaryIds.asSet()
        return dictionaryResources().map { it.dictionary }.filter { it.id in ids }
    }

    fun findDictionaryById(dictionaryId: Long): MemDbDictionary? {
        return dictionaryResourceById(dictionaryId)?.dictionary
    }

    fun saveDictionary(dictionary: MemDbDictionary): MemDbDictionary {
        val userId = requireNotNull(dictionary.userId) { "User id is required" }
        val resource = dictionaries.computeIfAbsent(userId) { ConcurrentHashMap() }
        val id = dictionary.id ?: idGenerator.nextDictionaryId()
        val res = dictionary.copy(
            id = id,
            changedAt = dictionary.changedAt ?: OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime()
        )
        resource[id] = DictionaryResource(res)
        dictionariesChanged = true
        return res
    }

    fun deleteDictionaryById(dictionaryId: Long): Boolean {
        val resource = dictionaries.map { it.value }.singleOrNull { it[dictionaryId] != null }
        return if (resource?.remove(dictionaryId) != null) {
            dictionariesChanged = true
            true
        } else {
            false
        }
    }

    fun countCards(): Long {
        return cards().map { 1L }.sum()
    }

    fun findCardsByDictionaryIds(dictionaryIds: Collection<Long>): Sequence<MemDbCard> {
        val ids = dictionaryIds.asSet()
        return dictionaryResources().filter { it.dictionary.id in ids }.flatMap { it.cards.values.asSequence() }
    }

    fun findCardsByDictionaryId(dictionaryId: Long): Sequence<MemDbCard> {
        return dictionaryResourceById(dictionaryId)?.cards?.values?.asSequence() ?: emptySequence()
    }

    fun findCardById(cardId: Long): MemDbCard? {
        return dictionaryResources().mapNotNull { it.cards[cardId] }.singleOrNull()
    }

    fun findCardsById(cardIds: Collection<Long>): Sequence<MemDbCard> {
        val ids = cardIds.toSet()
        return dictionaryResources()
            .flatMap { it.cards.entries.asSequence() }
            .filter { ids.contains(it.key) }
            .map { it.value }
    }

    fun counts(filter: (MemDbCard) -> Boolean = { true }): Map<Long, Long> {
        return dictionaryResources().mapNotNull {
            val dictionaryId = it.dictionary.id ?: return@mapNotNull null
            dictionaryId to it.cards.values.asSequence().filter(filter).count().toLong()
        }.toMap()
    }

    fun saveCard(card: MemDbCard): MemDbCard {
        val dictionaryId = requireNotNull(card.dictionaryId) { "No dictionaryId in the card $card" }
        val resource =
            requireNotNull(dictionaryResourceById(dictionaryId)) { "Can't find dictionary ${card.dictionaryId}" }
        val id = card.id ?: idGenerator.nextCardId()
        val res = card.copy(id = id, changedAt = card.changedAt ?: systemNow())
        resource.cards[id] = res
        cardsChanged = true
        return res
    }

    fun deleteCardById(cardId: Long): Boolean {
        val resource = dictionaryResources().filter { it.cards[cardId] != null }.singleOrNull()
        return if (resource?.cards?.remove(cardId) != null) {
            cardsChanged = true
            true
        } else {
            false
        }
    }

    private fun dictionaryResourceById(dictionaryId: Long): DictionaryResource? {
        return dictionaries.values.mapNotNull { it[dictionaryId] }.singleOrNull()
    }

    private fun dictionaryResources(): Sequence<DictionaryResource> {
        return dictionaries.values.asSequence().flatMap { it.values.asSequence() }
    }

    private fun cards(): Sequence<MemDbCard> {
        return dictionaries.values.asSequence().flatMap { it.values.asSequence() }
            .flatMap { it.cards.values.asSequence() }
    }

    private fun saveData() {
        if (databaseHomeDirectory == null) {
            return
        }
        if (cardsChanged) {
            val cards = cards().sortedBy { it.id }.toList()
            Paths.get(databaseHomeDirectory).resolve(CARDS_DB_FILE).outputStream().use {
                writeCards(cards, it)
            }
            cardsChanged = false
        }
        if (dictionariesChanged) {
            val dictionaries = dictionaryResources().map { it.dictionary }.sortedBy { it.id }.toList()
            Paths.get(databaseHomeDirectory).resolve(DICTIONARY_DB_FILE).outputStream().use {
                writeDictionaries(dictionaries, it)
            }
            dictionariesChanged = false
        }
        if (usersChanged) {
            Paths.get(databaseHomeDirectory).resolve(USERS_DB_FILE).outputStream().use {
                writeUsers(users.values, it)
            }
            usersChanged = false
        }
    }

    private data class DictionaryResource(
        val dictionary: MemDbDictionary,
        val cards: MutableMap<Long, MemDbCard> = ConcurrentHashMap(),
    )

    companion object {
        private const val USERS_DB_FILE = "users.csv"
        private const val DICTIONARY_DB_FILE = "dictionaries.csv"
        private const val CARDS_DB_FILE = "cards.csv"
        private const val CLASSPATH_PREFIX = "classpath:"

        private val logger = LoggerFactory.getLogger(MemDatabase::class.java)

        /**
         * Global dictionary store registry.
         */
        private val databaseRegistry = ConcurrentHashMap<String, MemDatabase>()

        /**
         * Back door for testing
         */
        @Synchronized
        fun clear() {
            save()
            databaseRegistry.clear()
        }

        @Synchronized
        private fun save() {
            databaseRegistry.forEach {
                it.value.saveData()
            }
        }

        init {
            timer("flush-database-to-disk", daemon = true, period = MemDbSettings.dataFlushPeriodInMs) {
                save()
            }
        }

        fun get(databaseLocation: String): MemDatabase {
            return databaseRegistry.computeIfAbsent(databaseLocation) { load(it) }
        }

        /**
         * Loads dictionary store from classpath or directory.
         */
        internal fun load(databaseLocation: String): MemDatabase {
            val fromClassPath = databaseLocation.startsWith(CLASSPATH_PREFIX)
            val dictionaryResources = if (fromClassPath) {
                loadDictionaryResourcesFromClassPath(databaseLocation)
            } else {
                loadDictionaryResourcesFromDirectory(databaseLocation)
            }
            val userResources = if (fromClassPath) {
                loadUserResourcesFromClassPath(databaseLocation)
            } else {
                loadUserResourcesFromDirectory(databaseLocation)
            }
            val maxDictionaryId = dictionaryResources.values.asSequence().flatMap { it.keys }.max()
            val maxCardId = dictionaryResources.values.asSequence()
                .flatMap { it.values.asSequence() }
                .flatMap { it.cards.map { card -> card.key } }
                .max()
            val ids = IdSequences(
                initDictionaryId = maxDictionaryId,
                initCardId = maxCardId,
            )
            return MemDatabase(
                dictionaries = dictionaryResources,
                users = userResources,
                idGenerator = ids,
                databaseHomeDirectory = if (fromClassPath) null else databaseLocation,
            )
        }

        private fun loadDictionaryResourcesFromDirectory(
            directoryDbLocation: String,
        ): MutableMap<String, MutableMap<Long, DictionaryResource>> {
            val cardFile = Paths.get(directoryDbLocation).resolve(CARDS_DB_FILE).toRealPath()
            val dictionaryFile = Paths.get(directoryDbLocation).resolve(DICTIONARY_DB_FILE).toRealPath()
            logger.info("Load cards data from file: <$cardFile>.")
            val cards = cardFile.inputStream().use {
                readCards(it)
            }
            logger.info("Load dictionaries data from file: <$dictionaryFile>.")
            val dictionaries = dictionaryFile.inputStream().use {
                readDictionaries(it)
            }
            return composeDictionaryData(directoryDbLocation, dictionaries, cards)
        }

        private fun loadDictionaryResourcesFromClassPath(
            classpathDbLocation: String,
        ): MutableMap<String, MutableMap<Long, DictionaryResource>> {
            val cardsFile = resolveClasspathResource(classpathDbLocation, CARDS_DB_FILE)
            val dictionariesFile = resolveClasspathResource(classpathDbLocation, DICTIONARY_DB_FILE)
            logger.info("Load cards data from classpath: <$cardsFile>.")
            val cards = checkNotNull(MemDatabase::class.java.getResourceAsStream(cardsFile)).use {
                readCards(it)
            }
            logger.info("Load dictionaries data from classpath: <$dictionariesFile>.")
            val dictionaries = checkNotNull(MemDatabase::class.java.getResourceAsStream(dictionariesFile)).use {
                readDictionaries(it)
            }
            return composeDictionaryData(classpathDbLocation, dictionaries, cards)
        }

        private fun loadUserResourcesFromDirectory(
            userDbLocation: String,
        ): MutableMap<String, MemDbUser> {
            val userFile = Paths.get(userDbLocation).resolve(USERS_DB_FILE).toRealPath()
            logger.info("Load users data from file: <$userFile>.")
            val users = userFile.inputStream().use {
                readUsers(it)
            }
            return users.associateByTo(ConcurrentHashMap()) { checkNotNull(it.id) }
        }

        private fun loadUserResourcesFromClassPath(
            classpathDbLocation: String,
        ): MutableMap<String, MemDbUser> {
            val usersFile = resolveClasspathResource(classpathDbLocation, USERS_DB_FILE)
            logger.info("Load cards data from classpath: <$usersFile>.")
            val users = checkNotNull(MemDatabase::class.java.getResourceAsStream(usersFile)).use {
                readUsers(it)
            }
            return users.associateByTo(ConcurrentHashMap()) { checkNotNull(it.id) }
        }

        private fun composeDictionaryData(
            dbLocation: String,
            dictionaries: List<MemDbDictionary>,
            cards: List<MemDbCard>
        ): MutableMap<String, MutableMap<Long, DictionaryResource>> {
            val dictionaryIds = mutableSetOf<Long>()
            val res = dictionaries
                .filter { it.userId != null }
                .groupBy { checkNotNull(it.userId) }
                .mapValues { (_, userDictionaries) ->
                    userDictionaries.map { dictionary ->
                        dictionaryIds.add(checkNotNull(dictionary.id))
                        val dictionaryCards = cards.asSequence()
                            .filter { it.dictionaryId == dictionary.id }
                            .associateByTo(ConcurrentHashMap()) { checkNotNull(it.id) }
                        DictionaryResource(dictionary, dictionaryCards)
                    }.associateByTo(ConcurrentHashMap()) { checkNotNull(it.dictionary.id) }
                }.toMap(ConcurrentHashMap())

            val unattachedDictionaryIds = dictionaries.asSequence().filter { it.userId == null }.map { it.id }.toList()
            val unattachedCardIds =
                cards.asSequence().filterNot { dictionaryIds.contains(it.dictionaryId) }.map { it.id }.toMutableSet()
            val dictionariesCount = res.values.sumOf { it.size }
            val cardsCount = res.values.flatMap { it.values }.sumOf { it.cards.size }

            logger.info("In the store=<$dbLocation> there are ${res.size} users, $dictionariesCount dictionaries and $cardsCount cards.")
            if (unattachedDictionaryIds.isNotEmpty()) {
                logger.warn("The ${unattachedDictionaryIds.size} dictionaries assigned to unknown users. ids = $unattachedDictionaryIds")
            }
            if (unattachedCardIds.isNotEmpty()) {
                logger.warn("The ${unattachedCardIds.size} cards assigned to unknown dictionaries. ids = $unattachedCardIds")
            }
            @Suppress("UNCHECKED_CAST")
            return res as MutableMap<String, MutableMap<Long, DictionaryResource>>
        }

        private fun readDictionaries(inputStream: InputStream): List<MemDbDictionary> =
            dictionaryCsvFormat(false).read(inputStream).use {
                it.records.map { record ->
                    MemDbDictionary(
                        id = record.value("id").toLong(),
                        name = record.value("name"),
                        userId = record.value("user_id"),
                        sourceLanguage = createMemDbLanguage(record.get("source_lang")),
                        targetLanguage = createMemDbLanguage(record.get("target_lang")),
                        details = fromJsonStringToMemDbDictionaryDetails(record.value("details")),
                        changedAt = LocalDateTime.parse(record.value("changed_at")),
                    )
                }
            }

        private fun readCards(inputStream: InputStream): List<MemDbCard> = cardCsvFormat(false).read(inputStream).use {
            it.records.map { record ->
                MemDbCard(
                    id = record.value("id").toLong(),
                    dictionaryId = record.value("dictionary_id").toLong(),
                    words = fromJsonStringToMemDbWords(record.value("words")),
                    details = fromJsonStringToMemDbCardDetails(record.value("details")),
                    answered = record.valueOrNull("answered")?.toInt(),
                    changedAt = LocalDateTime.parse(record.value("changed_at")),
                )
            }
        }

        private fun readUsers(inputStream: InputStream): List<MemDbUser> = userCsvFormat(false).read(inputStream).use {
            it.records.map { record ->
                MemDbUser(
                    id = record.value("id"),
                    createdAt = LocalDateTime.parse(record.value("created_at")),
                )
            }
        }

        private fun writeDictionaries(dictionaries: Collection<MemDbDictionary>, outputStream: OutputStream) =
            dictionaryCsvFormat(true).write(outputStream).use {
                dictionaries.forEach { dictionary ->
                    it.printRecord(
                        dictionary.id,
                        dictionary.name,
                        dictionary.userId,
                        dictionary.sourceLanguage.id,
                        dictionary.targetLanguage.id,
                        dictionary.detailsAsJsonString(),
                        dictionary.changedAt
                    )
                }
            }

        private fun writeCards(cards: Collection<MemDbCard>, outputStream: OutputStream) =
            cardCsvFormat(true).write(outputStream).use {
                cards.forEach { card ->
                    it.printRecord(
                        card.id,
                        card.dictionaryId,
                        card.wordsAsJsonString(),
                        card.detailsAsJsonString(),
                        card.answered,
                        card.changedAt,
                    )
                }
            }

        private fun writeUsers(users: Collection<MemDbUser>, outputStream: OutputStream) =
            userCsvFormat(true).write(outputStream).use {
                users.forEach { user ->
                    it.printRecord(
                        user.id,
                        user.createdAt,
                    )
                }
            }

        private fun dictionaryCsvFormat(withHeader: Boolean): CSVFormat = CSVFormat.DEFAULT.builder()
            .setHeader(
                "id",
                "name",
                "user_id",
                "source_lang",
                "target_lang",
                "details",
                "changed_at",
            )
            .setSkipHeaderRecord(!withHeader)
            .build()

        private fun cardCsvFormat(withHeader: Boolean): CSVFormat = CSVFormat.DEFAULT.builder()
            .setHeader(
                "id",
                "dictionary_id",
                "words",
                "details",
                "answered",
                "changed_at",
            )
            .setSkipHeaderRecord(!withHeader)
            .build()

        private fun userCsvFormat(withHeader: Boolean): CSVFormat = CSVFormat.DEFAULT.builder()
            .setHeader(
                "id",
                "created_at",
            )
            .setSkipHeaderRecord(!withHeader)
            .build()

        private fun CSVRecord.value(key: String): String =
            requireNotNull(get(key)) { "null value for '$key'. record = $this" }

        private fun CSVRecord.valueOrNull(key: String): String? = get(key)?.takeIf { it.isNotBlank() }

        private fun CSVFormat.write(outputStream: OutputStream): CSVPrinter =
            print(outputStream.bufferedWriter(charset = Charsets.UTF_8))

        private fun CSVFormat.read(inputStream: InputStream): CSVParser =
            parse(inputStream.bufferedReader(charset = Charsets.UTF_8))

        private fun <X> Collection<X>.asSet(): Set<X> = if (this is Set<X>) this else toSet()

        private fun resolveClasspathResource(classpathDir: String, classpathFilename: String): String =
            "${classpathDir.substringAfter(CLASSPATH_PREFIX)}/$classpathFilename".replace("//", "/")

    }
}