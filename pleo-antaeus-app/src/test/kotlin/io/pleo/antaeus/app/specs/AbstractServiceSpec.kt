package io.pleo.antaeus.app.specs

import io.pleo.antaeus.data.AntaeusConnectionProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.DatabaseHelper

abstract class AbstractServiceSpec {

    companion object {
        private val db = DatabaseHelper.db("jdbc:sqlite:/tmp/data.test.db", "org.sqlite.JDBC")

        // Set up data access layer.
        val connectionProvider = AntaeusConnectionProvider(db)
        val dal = AntaeusDal(connectionProvider)
    }
}