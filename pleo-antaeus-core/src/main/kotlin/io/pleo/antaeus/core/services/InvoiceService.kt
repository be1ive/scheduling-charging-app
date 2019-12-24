/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.time.LocalDate

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetchUnpaidInvoicesAt(date: LocalDate): List<Invoice> {
       return dal.fetchUnpaidInvoicesAt(date)
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun create(amount: Money, date: LocalDate, customerId: Int): Invoice {
        val customer = dal.fetchCustomer(customerId)
            ?: throw CustomerNotFoundException(customerId)
        return dal.createInvoice(amount, date, customer)
    }

    fun update(invoice: Invoice): Invoice {
        return dal.updateInvoice(invoice)
    }
}
