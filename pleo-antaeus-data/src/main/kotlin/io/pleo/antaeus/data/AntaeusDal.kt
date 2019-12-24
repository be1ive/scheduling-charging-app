/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
import org.jetbrains.exposed.sql.*
import java.time.LocalDate
import java.time.ZoneOffset

class AntaeusDal(connectionProvider: ConnectionProvider): AbstractDal(connectionProvider) {

    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return inTransaction {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return inTransaction {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchUnpaidInvoicesAt(date: LocalDate): List<Invoice> {
        return inTransaction {
            InvoiceTable
                .selectAll()
                .andWhere { InvoiceTable.date.lessEq(date)
                    .and(InvoiceTable.status.eq(InvoiceStatus.PENDING.toString())) }
                .map { it.toInvoice() }
        }
    }

    fun createInvoice(amount: Money, date: LocalDate, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice {
        val id = inTransaction {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.amount] = amount.minorValue
                    it[this.date] = date
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                    it[this.paidAt] = null
                } get InvoiceTable.id
        }

        return fetchInvoice(id)!!
    }

    fun updateInvoice(invoice: Invoice): Invoice {
        val count = inTransaction {
            // Update the invoice and returns its new id.
            InvoiceTable
                .update ({ InvoiceTable.id.eq(invoice.id) }) {
                    it[this.amount] = invoice.amount.minorValue
                    it[this.date] = invoice.date
                    it[this.currency] = invoice.amount.currency.toString()
                    it[this.status] = invoice.status.toString()
                    it[this.customerId] = invoice.customerId
                    it[this.paidAt] = invoice.paidAt?.atZone(ZoneOffset.UTC)?.toLocalDateTime()
                }
        }

        if (count != 1)
            throw DatabaseException("Expected 1 row(s) to be updated")

        return fetchInvoice(invoice.id)!!
    }

    fun fetchCustomer(id: Int): Customer? {
        return inTransaction {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return inTransaction {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(baseCurrency: Currency): Customer {
        val id = inTransaction {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.baseCurrency] = baseCurrency.toString()
            } get CustomerTable.id
        }

        return fetchCustomer(id)!!
    }

    fun fetchPocket(id: Int): Pocket? {
        return inTransaction {
            PocketTable
                .select { PocketTable.id.eq(id) }
                .firstOrNull()
                ?.toPocket()
        }
    }

    fun fetchBasePocketFor(customer: Customer): Pocket? {
        return inTransaction {
            InvoiceTable
                .select { PocketTable.currency.eq(customer.baseCurrency.toString()) }
                .firstOrNull()
                ?.toPocket()
        }
    }

    fun createPocket(currency: Currency, customer: Customer): Pocket {
        val id = inTransaction {
            // Insert the pocket and return its new id.
            PocketTable.insert {
                it[this.amount] = 0
                it[this.currency] = currency.toString()
                it[this.customerId] = customer.id
            } get PocketTable.id
        }

        return fetchPocket(id)!!
    }

    fun updatePocket(pocket: Pocket): Pocket {
        val count = inTransaction {
            // Update the pocket and returns its new id.
            PocketTable
                .update ({ PocketTable.id.eq(pocket.id) }) {
                    it[this.amount] = pocket.balance.minorValue
                    it[this.currency] = pocket.balance.currency.toString()
                    it[this.customerId] = pocket.customerId
                }
        }

        if (count != 1)
            throw DatabaseException("Expected 1 row(s) to be updated")

        return fetchPocket(pocket.id)!!
    }
}
