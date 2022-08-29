package com.gitlab.sszuev.flashcards.dbpg

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

internal fun <R> Database.execute(statement: Transaction.() -> R): R {
    return transaction(this, statement)
}

internal fun <R> Database.execute(statement: Transaction.() -> R, handleError: Exception.() -> R): R {
    return try {
        transaction(this, statement)
    } catch (ex: SQLException) {
        handleError(ex)
    }
}