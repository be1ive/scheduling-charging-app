/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.ResultRow
import java.time.ZoneOffset

fun ResultRow.toInvoice(): Invoice = Invoice(
    id = this[InvoiceTable.id],
    amount = Money(
        minorValue = this[InvoiceTable.amount],
        currency = Currency.valueOf(this[InvoiceTable.currency])
    ),
    status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
    customerId = this[InvoiceTable.customerId],
    date = this[InvoiceTable.date],
    paidAt = this[InvoiceTable.paidAt]?.toInstant(ZoneOffset.UTC)
)

fun ResultRow.toPocket(): Pocket = Pocket(
    id = this[PocketTable.id],
    balance = Money(
        minorValue = this[PocketTable.amount],
        currency = Currency.valueOf(this[PocketTable.currency])
    ),
    customerId = this[PocketTable.customerId]
)

fun ResultRow.toCustomer(): Customer = Customer(
    id = this[CustomerTable.id],
    baseCurrency = Currency.valueOf(this[CustomerTable.baseCurrency])
)
