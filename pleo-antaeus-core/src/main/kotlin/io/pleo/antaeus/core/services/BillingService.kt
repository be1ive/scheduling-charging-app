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

    fun bill(invoice: Invoice) {
        connectionProvider.inTransaction {
            invoiceService.fetch(invoice.id)
                .takeIf { it.isPending() }
                ?.let { invoice ->
                    process(invoice).also {
                        if (tryCharge(invoice)) {
                            payAndCreateNext(invoice)
                        } else {
                            cancel(invoice)
                        }
                    }
                }
        }
    }

    private fun tryCharge(invoice: Invoice): Boolean {
        return try {
            if (paymentProvider.charge(invoice)) {
                true
            } else {
                logger.warn { "Customer insufficient funds to pay for invoice ${invoice.id}" }
                throw CustomerInsufficientFundsException(invoice.customerId)
            }
        } catch (e: InvoiceAlreadyChargedException) {
            logger.warn { "Customer was already charged for invoice ${invoice.id}" }
            true
        } catch (e: CustomerNotFoundException) {
            logger.error(e) { "Customer was not found for invoice ${invoice.id}" }
            false
        }
    }

    private fun process(invoice: Invoice) {
        // update current invoice to process
        invoiceService.update(
            invoice.process())
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

    private fun cancel(invoice: Invoice) {
        // update current invoice to cancel
        invoiceService.update(
            invoice.cancel())
    }
}