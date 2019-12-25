package io.pleo.antaeus.job

import io.pleo.antaeus.core.exceptions.*
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDate
import kotlin.concurrent.fixedRateTimer

private val logger = KotlinLogging.logger {}

class BillingJob(
    private val invoiceService: InvoiceService,
    private val billingService: BillingService
): AntaeusJob() {

    override fun run() {
        fixedRateTimer(
            javaClass.name,
            daemon = true,
            period = Duration.ofMinutes(30).toMillis()) {
            logger.info("Start billing charge...")
            try {
                runOnce()
            } catch (ex: RuntimeException) {
                logger.error("Job failed due to", ex)
            }
            logger.info("Finish billing charge")
        }
    }

    fun runOnce() {
        val invoices = invoiceService.fetchUnpaidInvoicesAt(LocalDate.now())
        invoices.forEach { invoice ->
            try {
                billingService.bill(invoice)
            } catch (e: Exception) {
                logger.error(e) { "Unhandled exception while charging for invoice ${invoice.id}" }
            }
        }
    }

}