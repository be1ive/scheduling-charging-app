package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class AntaeusConnectionProvider(private val db: Database) : ConnectionProvider() {

    override fun <T> inTransaction(block: () -> T): T {
        return transaction(db) {
            try {
                block()
            } catch (e: SQLException) {
                throw DatabaseException("Exception due to database issue", e)
            }
        }
    }
}