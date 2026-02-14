package com.gitlab.sszuev.flashcards.dbpg

import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.SQLException

internal fun <R> Database.execute(statement: Transaction.() -> R): R {
    return transaction(db = this, statement = statement)
}

internal fun <R> Database.execute(statement: Transaction.() -> R, handleError: Exception.() -> R): R {
    return try {
        transaction(db = this, statement = statement)
    } catch (ex: SQLException) {
        handleError(ex)
    }
}