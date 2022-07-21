package com.gitlab.sszuev.flashcards.config

import org.slf4j.LoggerFactory
import java.util.*

private val logger = LoggerFactory.getLogger(BaseConfig::class.java)

/**
 * Base config abstraction.
 * It implements the following order for looking-for operations:
 * - system properties,
 * - system environment variables
 * - client defined properties file, i.e. [configResource]
 * - client default values
 */
abstract class BaseConfig(
    private val configResource: String = "/application.properties"
) {

    protected val properties: Properties by lazy {
        loadProperties()
    }

    protected fun loadProperties(): Properties {
        val res = Properties()
        val stream = BaseConfig::class.java.getResourceAsStream(configResource)
        if (stream == null) {
            logger.warn("Can't find $configResource.")
            return res
        }
        stream.use {
            res.load(it)
        }
        return res
    }

    protected inline fun <reified X : Any> getValue(key: String, default: X): X {
        var res = System.getProperty(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        res = System.getenv(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        res = properties.getProperty(key)
        if (res != null) {
            return toTypedValue(res, default)
        }
        return default
    }

    protected inline fun <reified X : Any> toTypedValue(value: String, typed: X): X {
        if (X::class == String::class) {
            return value as X
        }
        if (X::class == Int::class) {
            return value.toInt() as X
        }
        if (X::class == Long::class) {
            return value.toLong() as X
        }
        throw IllegalStateException("Wrong type: ${typed::class.simpleName}, value: $value")
    }
}