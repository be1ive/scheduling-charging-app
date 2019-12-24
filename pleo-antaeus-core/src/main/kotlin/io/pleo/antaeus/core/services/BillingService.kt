package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.*
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.ConnectionProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

class BillingService(
    private val connectionProvider: ConnectionProvider,
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
) {
    private val maxRetries = 3

    fun chargeAll() {
        val invoices = invoiceService.fetchUnpaidInvoicesAt(LocalDate.now())
        invoices.forEach {
            try {
                connectionProvider.inTransaction {
                    invoiceService.fetch(it.id)
                        .takeIf { it.isPending() }
                        ?.let { invoice ->
                            tryCharge(invoice)
                                .takeIf { it }
                                ?.let { payAndCreateNext(invoice) }
                        }
                }
            } catch (e: Exception) {
                logger.error(e) { "Unhandled exception while charging for invoice ${it.id}" }
            }
        }
    }

    private fun tryCharge(invoice: Invoice): Boolean {
        var tries = 0
        while (tries++ <= maxRetries) {
            try {
                return paymentProvider.charge(invoice).also {
                    if (!it) logger.warn { "Customer insufficient funds to pay for invoice ${invoice.id}" }
                }
            } catch (e: InvoiceAlreadyChargedException) {
                logger.warn { "Customer was already charged for invoice ${invoice.id}" }
                return true
            } catch (e: CustomerNotFoundException) {
                logger.error(e) { "Customer was not found for invoice ${invoice.id}" }
                return false
            } catch (e: CurrencyMismatchException) {
                logger.error(e) { "Customer currency not matching invoice ${invoice.id}" }
                return false
            } catch (e: PocketCurrencyMismatchException) {
                logger.error(e) { "Customer pocket currency not matching invoice ${invoice.id}" }
                return false
            } catch (e: NetworkException) {
                logger.warn { "Network issue while charging for invoice ${invoice.id}" }
            }
        }
        return false
    }

    private fun payAndCreateNext(invoice: Invoice) {
        // update current invoice to paid
        invoiceService.update(
            invoice.pay())

        // create next invoice
        invoiceService.create(
            invoice.amount,
            invoice.date
                .withDayOfMonth(1)
                .plusMonths(1),
            invoice.customerId)
    }
}