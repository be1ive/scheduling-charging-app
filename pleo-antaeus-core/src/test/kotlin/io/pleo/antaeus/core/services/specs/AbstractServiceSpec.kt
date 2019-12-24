package io.pleo.antaeus.core.services.specs

import io.pleo.antaeus.data.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

abstract class AbstractServiceSpec {

    companion object {
        private val tables = arrayOf(InvoiceTable, CustomerTable, PocketTable)

        // Connect to the database and create the needed tables. Drop any existing data.
        private val db = Database
            .connect("jdbc:sqlite:/tmp/data.test.db", "org.sqlite.JDBC")
            .also {
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                transaction(it) {
                    addLogger(StdOutSqlLogger)
                    // Drop all existing tables to ensure a clean slate on each run
                    SchemaUtils.drop(*tables)
                    // Create all tables
                    SchemaUtils.create(*tables)
                }
            }

        // Set up data access layer.
        val connectionProvider = AntaeusConnectionProvider(db)
        val dal = AntaeusDal(connectionProvider)
    }
}