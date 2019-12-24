/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
    val amount = long("amount")
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
    val date = date("date")
    val paidAt = datetime("paid_at").nullable()
}

object PocketTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
    val amount = long("amount")
    val customerId = reference("customer_id", CustomerTable.id)
}

object CustomerTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val baseCurrency = varchar("base_currency", 3)
}
