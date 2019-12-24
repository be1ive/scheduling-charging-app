package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusConnectionProvider
import io.pleo.antaeus.data.ConnectionProvider
import io.pleo.antaeus.models.Invoice

class LocalPaymentProvider(
    private val connectionProvider: ConnectionProvider,
    private val customerService: CustomerService,
    private val invoiceService: InvoiceService,
    private val pocketService: PocketService
): PaymentProvider {

    override fun charge(invoice: Invoice): Boolean {
        return connectionProvider.inTransaction {
            val invoice = invoiceService.fetch(invoice.id)
            val customer = customerService.fetch(invoice.customerId)

            if (customer.baseCurrency != invoice.amount.currency)
                throw CurrencyMismatchException(invoice.id, invoice.customerId)

            val pocket = pocketService.fetchBasePocketFor(invoice.customerId)

            val updatedPocket = pocketService.update(
                pocket.debit(invoice.amount))

            true
        }
    }
}