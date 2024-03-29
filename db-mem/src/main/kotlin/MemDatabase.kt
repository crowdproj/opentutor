package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.common.systemNow
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbCard
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbDictionary
import com.gitlab.sszuev.flashcards.dbmem.dao.MemDbUser
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

/**
 * A dictionary store, attached to file system or classpath.
 * In the first case it is persistent.
 */
class MemDatabase private constructor(
    private val idGenerator: IdSequences,
    private val resources: MutableMap<Long, UserResource>,
    private val databaseHomeDirectory: String?,
) {

    @Volatile
    private var dictionariesChanged = false

    @Volatile
    private var cardsChanged = false

    @Volatile
    private var usersChanged = false

    fun countUsers(): Long {
        return resources.size.toLong()
    }

    fun findUsers(): Sequence<MemDbUser> {
        return resources.asSequence().map { it.value.user }
    }

    fun findUserByUuid(userUuid: UUID): MemDbUser? {
        return resources.asSequence().map { it.value.user }.singleOrNull { it.uuid == userUuid }
    }

    fun saveUser(user: MemDbUser): MemDbUser {
        require(user.id != null || user.changedAt == null)
        val id = user.id ?: idGenerator.nextUserId()
        val res = user.copy(id = id, changedAt = systemNow())
        resources[id] = UserResource(res)
        usersChanged = true
        return res
    }

    fun countDictionaries(): Long {
        return resources.asSequence().map { it.value.dictionaries.size.toLong() }.sum()
    }

    fun findDictionariesByUserId(userId: Long): Sequence<MemDbDictionary> {
        return resources[userId]?.dictionaries?.asSequence()?.map { it.value.dictionary } ?: emptySequence()
    }

    fun findDictionariesByIds(dictionaryIds: Collection<Long>): Sequence<MemDbDictionary> {
        val ids = dictionaryIds.asSet()
        return dictionaryResources().map { it.dictionary }.filter { it.id in ids }
    }

    fun findDictionaryById(dictionaryId: Long): MemDbDictionary? {
        return dictionaryResourceById(dictionaryId)?.dictionary
    }

    fun saveDictionary(dictionary: MemDbDictionary): MemDbDictionary {
        val resource =
            requireNotNull(resources[dictionary.userId]) { "Unknown user ${dictionary.userId}" }
        val id = dictionary.id ?: idGenerator.nextDictionaryId()
        val res = dictionary.copy(id = id, changedAt = dictionary.changedAt ?: OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime())
        resource.dictionaries[id] = DictionaryResource(res)
        dictionariesChanged = true
        return res
    }

    fun deleteDictionaryById(dictionaryId: Long): Boolean {
        val resource = resources.map { it.value.dictionaries }.singleOrNull { it[dictionaryId] != null }
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
        return resources.values.mapNotNull { it.dictionaries[dictionaryId] }.singleOrNull()
    }

    private fun dictionaryResources(): Sequence<DictionaryResource> {
        return resources.values.asSequence().flatMap { it.dictionaries.values.asSequence() }
    }

    private fun cards(): Sequence<MemDbCard> {
        return resources.values.asSequence().flatMap { it.dictionaries.values.asSequence() }
            .flatMap { it.cards.values.asSequence() }
    }

    private fun users(): Sequence<MemDbUser> {
        return resources.values.asSequence().map { it.user }
    }

    private fun saveData() {
        if (databaseHomeDirectory == null) {
            return
        }
        if (usersChanged) {
            val users = users().sortedBy { it.id }.toList()
            Paths.get(databaseHomeDirectory).resolve(usersDbFile).outputStream().use {
                writeUsers(users, it)
            }
            usersChanged = false
        }
        if (cardsChanged) {
            val cards = cards().sortedBy { it.id }.toList()
            Paths.get(databaseHomeDirectory).resolve(cardsDbFile).outputStream().use {
                writeCards(cards, it)
            }
            cardsChanged = false
        }
        if (dictionariesChanged) {
            val dictionaries = dictionaryResources().map { it.dictionary }.sortedBy { it.id }.toList()
            Paths.get(databaseHomeDirectory).resolve(dictionariesDbFile).outputStream().use {
                writeDictionaries(dictionaries, it)
            }
            dictionariesChanged = false
        }
    }

    private data class UserResource(
        val user: MemDbUser,
        val dictionaries: MutableMap<Long, DictionaryResource> = ConcurrentHashMap(),
    )

    private data class DictionaryResource(
        val dictionary: MemDbDictionary,
        val cards: MutableMap<Long, MemDbCard> = ConcurrentHashMap(),
    )

    companion object {
        private const val usersDbFile = "users.csv"
        private const val dictionariesDbFile = "dictionaries.csv"
        private const val cardsDbFile = "cards.csv"

        private const val classpathPrefix = "classpath:"
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
            val fromClassPath = databaseLocation.startsWith(classpathPrefix)
            val res = if (fromClassPath) {
                loadDatabaseResourcesFromClassPath(databaseLocation)
            } else {
                loadDatabaseResourcesFromDirectory(databaseLocation)
            }
            val maxUserId = res.keys.max()
            val maxDictionaryId = res.values.asSequence().flatMap { it.dictionaries.keys.asSequence() }.max()
            val maxCardId = res.values.asSequence()
                .flatMap { it.dictionaries.asSequence() }
                .flatMap { it.value.cards.keys.asSequence() }
                .max()
            val ids = IdSequences(
                initUserId = maxUserId,
                initDictionaryId = maxDictionaryId,
                initCardId = maxCardId,
            )
            return MemDatabase(
                resources = res,
                idGenerator = ids,
                databaseHomeDirectory = if (fromClassPath) null else databaseLocation,
            )
        }

        private fun loadDatabaseResourcesFromDirectory(
            directoryDbLocation: String,
        ): MutableMap<Long, UserResource> {
            val usersFile = Paths.get(directoryDbLocation).resolve(usersDbFile).toRealPath()
            val cardsFile = Paths.get(directoryDbLocation).resolve(cardsDbFile).toRealPath()
            val dictionariesFile = Paths.get(directoryDbLocation).resolve(dictionariesDbFile).toRealPath()
            logger.info("Load users data from file: <$usersFile>.")
            val users = usersFile.inputStream().use {
                readUsers(it)
            }
            logger.info("Load cards data from file: <$cardsFile>.")
            val cards = cardsFile.inputStream().use {
                readCards(it)
            }
            logger.info("Load dictionaries data from file: <$dictionariesFile>.")
            val dictionaries = dictionariesFile.inputStream().use {
                readDictionaries(it)
            }
            return composeDatabaseData(directoryDbLocation, users, dictionaries, cards)
        }

        private fun loadDatabaseResourcesFromClassPath(
            classpathDbLocation: String,
        ): MutableMap<Long, UserResource> {
            val usersFile = resolveClasspathResource(classpathDbLocation, usersDbFile)
            val cardsFile = resolveClasspathResource(classpathDbLocation, cardsDbFile)
            val dictionariesFile = resolveClasspathResource(classpathDbLocation, dictionariesDbFile)
            logger.info("Load users data from classpath: <$usersFile>.")
            val users = checkNotNull(MemDatabase::class.java.getResourceAsStream(usersFile)).use {
                readUsers(it)
            }
            logger.info("Load cards data from classpath: <$cardsFile>.")
            val cards = checkNotNull(MemDatabase::class.java.getResourceAsStream(cardsFile)).use {
                readCards(it)
            }
            logger.info("Load dictionaries data from classpath: <$dictionariesFile>.")
            val dictionaries = checkNotNull(MemDatabase::class.java.getResourceAsStream(dictionariesFile)).use {
                readDictionaries(it)
            }
            return composeDatabaseData(classpathDbLocation, users, dictionaries, cards)
        }

        private fun composeDatabaseData(
            dbLocation: String,
            users: List<MemDbUser>,
            dictionaries: List<MemDbDictionary>,
            cards: List<MemDbCard>
        ): MutableMap<Long, UserResource> {
            val res = users.map { user ->
                val userDictionaries = dictionaries.asSequence()
                    .filter { it.userId == user.id }
                    .map { dictionary ->
                        val dictionaryCards = cards.asSequence()
                            .filter { it.dictionaryId == dictionary.id }
                            .associateByTo(ConcurrentHashMap()) { checkNotNull(it.id) }
                        DictionaryResource(dictionary, dictionaryCards)
                    }
                    .associateByTo(ConcurrentHashMap()) { checkNotNull(it.dictionary.id) }
                UserResource(user, userDictionaries)
            }.associateByTo(ConcurrentHashMap()) { checkNotNull(it.user.id) }

            val unattachedDictionaryIds = dictionaries.asSequence().map { it.id }.toMutableSet()
            val unattachedCardIds = cards.asSequence().map { it.id }.toMutableSet()
            val dictionariesCount = res.values.asSequence()
                .flatMap { it.dictionaries.keys.asSequence() }
                .onEach { unattachedDictionaryIds.remove(it) }
                .count()
            val cardsCount = res.values.asSequence()
                .flatMap { it.dictionaries.values.asSequence() }
                .flatMap { it.cards.keys.asSequence() }
                .onEach { unattachedCardIds.remove(it) }
                .count()

            logger.info("In the store=<$dbLocation> there are ${res.size} users, $dictionariesCount dictionaries and $cardsCount cards.")
            if (unattachedDictionaryIds.isNotEmpty()) {
                logger.warn("The ${unattachedDictionaryIds.size} dictionaries assigned to unknown users. ids = $unattachedDictionaryIds")
            }
            if (unattachedCardIds.isNotEmpty()) {
                logger.warn("The ${unattachedCardIds.size} cards assigned to unknown dictionaries. ids = $unattachedCardIds")
            }
            return res
        }

        private fun readUsers(inputStream: InputStream): List<MemDbUser> = userCsvFormat(false).read(inputStream).use {
            it.records.map { record ->
                MemDbUser(
                    id = record.value("id").toLong(),
                    uuid = UUID.fromString(record.value("uuid")),
                    details = fromJsonStringToMemDbUserDetails(record.value("details")),
                    changedAt = LocalDateTime.parse(record.value("changed_at")),
                )
            }
        }

        private fun readDictionaries(inputStream: InputStream): List<MemDbDictionary> =
            dictionaryCsvFormat(false).read(inputStream).use {
                it.records.map { record ->
                    MemDbDictionary(
                        id = record.value("id").toLong(),
                        name = record.value("name"),
                        userId = record.value("user_id").toLong(),
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

        private fun writeUsers(users: Collection<MemDbUser>, outputStream: OutputStream) =
            userCsvFormat(true).write(outputStream).use {
                users.forEach { user ->
                    it.printRecord(
                        user.id,
                        user.uuid,
                        user.detailsAsJsonString(),
                        user.changedAt,
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
                        card.changedAt
                    )
                }
            }

        private fun userCsvFormat(withHeader: Boolean): CSVFormat {
            return CSVFormat.DEFAULT.builder()
                .setHeader(
                    "id",
                    "uuid",
                    "details",
                    "changed_at",
                )
                .setSkipHeaderRecord(!withHeader)
                .build()
        }

        private fun dictionaryCsvFormat(withHeader: Boolean): CSVFormat {
            return CSVFormat.DEFAULT.builder()
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
        }

        private fun cardCsvFormat(withHeader: Boolean): CSVFormat {
            return CSVFormat.DEFAULT.builder()
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
        }

        private fun CSVRecord.value(key: String): String {
            return requireNotNull(get(key)) { "null value for '$key'. record = $this" }
        }

        private fun CSVRecord.valueOrNull(key: String): String? {
            return get(key)?.takeIf { it.isNotBlank() }
        }

        private fun CSVFormat.write(outputStream: OutputStream): CSVPrinter {
            return print(outputStream.bufferedWriter(charset = Charsets.UTF_8))
        }

        private fun CSVFormat.read(inputStream: InputStream): CSVParser {
            return parse(inputStream.bufferedReader(charset = Charsets.UTF_8))
        }

        private fun <X> Collection<X>.asSet(): Set<X> {
            return if (this is Set<X>) this else toSet()
        }

        private fun resolveClasspathResource(classpathDir: String, classpathFilename: String): String {
            return "${classpathDir.substringAfter(classpathPrefix)}/$classpathFilename".replace("//", "/")
        }

    }
}