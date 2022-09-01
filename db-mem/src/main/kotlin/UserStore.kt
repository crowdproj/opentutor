package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.dbmem.dao.IdSequences
import com.gitlab.sszuev.flashcards.dbmem.dao.User
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import java.io.Reader
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.writer
import kotlin.streams.asSequence


class UserStore private constructor(
    private val resources: MutableMap<UUID, User>,
    private val file: Path?,
    dbConfig: MemDbConfig,
) {

    /**
     * A queue to flushing data to disk.
     */
    @Volatile
    private var usersToFlush: List<User>? = null

    init {
        timer("flush-users-to-disk", daemon = true, period = dbConfig.dataFlushPeriodInMs) {
            val users = usersToFlush
            if (users != null) {
                usersToFlush = null
                file!!.writer(StandardCharsets.UTF_8).use {
                    write(it, users)
                }
            }
        }
    }

    val size: Long
        get() = resources.size.toLong()

    val keys: Set<UUID>
        get() = resources.keys

    operator fun get(id: UUID): User? {
        return resources[id]
    }

    operator fun plus(user: User): UserStore {
        resources[user.uuid] = user
        return this
    }

    /**
     * Flushes the specified dictionary data to disk.
     * It works only if this store is attached to physical directory.
     */
    fun flush() {
        if (file == null) {
            return
        }
        usersToFlush = resources.values.toList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserStore::class.java)

        /**
         * Global dictionary store registry.
         */
        private val stores = ConcurrentHashMap<String, UserStore>()

        /**
         * Back door for testing
         */
        internal fun clear() {
            stores.clear()
        }

        /**
         * Loads user store from directory.
         * @param [location][Path] - path to directory
         * @param [ids][IdSequences]
         * @param [dbConfig][MemDbConfig]
         */
        fun load(
            location: Path,
            ids: IdSequences = IdSequences.globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig()
        ): UserStore {
            return stores.computeIfAbsent(location.toString()) {
                val data = loadDatabaseFromDirectory(location, ids)
                UserStore(
                    resources = data.second, file = data.first, dbConfig = dbConfig
                )
            }
        }

        /**
         * Loads user store from classpath or directory.
         * @param [location][String] - either dir path or classpath to dir with data
         * @param [ids][IdSequences]
         * @param [dbConfig][MemDbConfig]
         */
        fun load(
            location: String,
            ids: IdSequences = IdSequences.globalIdsGenerator,
            dbConfig: MemDbConfig = MemDbConfig()
        ): UserStore {
            return stores.computeIfAbsent(location) {
                val data = loadDatabase(it, ids)
                UserStore(
                    resources = data.second, file = data.first, dbConfig = dbConfig
                )
            }
        }

        private fun loadDatabase(location: String, ids: IdSequences): Pair<Path?, MutableMap<UUID, User>> {
            return if (location.startsWith("classpath:")) {
                null to loadDatabaseFromClassPath(location.removePrefix("classpath:"), ids)
            } else loadDatabaseFromDirectory(location, ids)
        }

        private fun loadDatabaseFromClassPath(
            classpathLocation: String,
            ids: IdSequences
        ): MutableMap<UUID, User> {
            logger.info("Load users from classpath: $classpathLocation.")
            val file = requireNotNull(UserStore::class.java.getResourceAsStream(classpathLocation)) {
                "Can't find classpath directory $classpathLocation."
            }.bufferedReader(Charsets.UTF_8).use { br ->
                // java Stream to Sequence and then to List due to compilation error with type inferencing
                br.lines().asSequence().toList()
            }.filter {
                return@filter if (!it.endsWith(".csv")) {
                    logger.debug("Not a csv file: $it.")
                    false
                } else {
                    true
                }
            }.singleOrNull()
            if (file == null) {
                logger.error("Can't find csv file.")
                return ConcurrentHashMap()
            }
            val res = requireNotNull(UserStore::class.java.getResourceAsStream("$classpathLocation/$file")) {
                "Can't find classpath resource $classpathLocation/$file."
            }.use {
                ConcurrentHashMap(parse(it.bufferedReader(Charsets.UTF_8), ids))
            }
            logger.info("For location=$classpathLocation there are ${res.size} users loaded.")
            return res
        }

        private fun loadDatabaseFromDirectory(
            directoryLocation: String,
            ids: IdSequences
        ): Pair<Path?, MutableMap<UUID, User>> {
            logger.info("Load users from directory: $directoryLocation.")
            val dir = toDirectory(directoryLocation)
            return loadDatabaseFromDirectory(dir, ids)
        }

        private fun loadDatabaseFromDirectory(
            dir: Path,
            ids: IdSequences
        ): Pair<Path?, MutableMap<UUID, User>> {
            val file: Path? = Files.newDirectoryStream(dir).use { d ->
                d.mapNotNull { f ->
                    if (!f.isRegularFile()) {
                        logger.warn("Not a file: $f.")
                        return@mapNotNull null
                    }
                    if (!f.fileName.toString().endsWith(".csv")) {
                        logger.debug("Not a csv file: $f.")
                        return@mapNotNull null
                    }
                    f
                }.singleOrNull()
            }
            if (file == null) {
                logger.error("Can't find csv file.")
                return null to ConcurrentHashMap()
            }
            val res = Files.newBufferedReader(file, StandardCharsets.UTF_8).use {
                ConcurrentHashMap(parse(it, ids))
            }
            logger.info("For location=$dir there are ${res.size} users loaded.")
            return file to res
        }

        private fun parse(reader: Reader, ids: IdSequences): Map<UUID, User> {
            return CSVFormat.DEFAULT.builder()
                .setHeader("id", "uuid", "role")
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader).use { parser ->
                    parser.records.associate { record ->
                        val idStr = record.get("id")
                        val id = if (idStr.isNotBlank()) {
                            idStr.toLong()
                        } else {
                            ids.nextUserId()
                        }
                        val uuid = UUID.fromString(record.get("uuid"))
                        val roleStr = record.get("role")
                        val role = if (roleStr.isNotBlank()) {
                            roleStr.toInt()
                        } else {
                            42
                        }
                        uuid to User(id, uuid, role)
                    }
                }
        }

        private fun write(writer: Writer, users: Collection<User>) {
            val format = CSVFormat.DEFAULT.builder()
                .setHeader("id", "uuid", "role")
                .build()
            CSVPrinter(writer, format).use { printer ->
                users.forEach {
                    printer.printRecord(it.id, it.uuid, it.role)
                }
            }
        }

        private fun toDirectory(directoryLocation: String): Path {
            return Paths.get(directoryLocation).createDirectories().toRealPath().requireDirectory()
        }

        private fun Path.requireDirectory(): Path {
            require(isDirectory()) { "Not a directory: $this." }
            return this
        }
    }
}