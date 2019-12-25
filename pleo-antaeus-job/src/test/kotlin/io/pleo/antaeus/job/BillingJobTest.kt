package io.pleo.antaeus.job

import io.mockk.*
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.random.Random

class BillingJobTest {

    private val billingService = mockk<BillingService>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingJob = BillingJob(
        invoiceService,
        billingService)

    @Test
    fun `will successfully call billing service`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)

        every {
            billingService.bill(any())
        } just Runs

        // when
        billingJob.runOnce()

        // then
        verify { billingService.bill(invoice) }
    }

    @Test
    fun `will successfully call billing service if one billing action throws error`() {
        // given
        val invoice1 = anInvoice(InvoiceStatus.PENDING)
        val invoice2 = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice1, invoice2)

        every {
            billingService.bill(invoice1)
        } throws NetworkException()

        every {
            billingService.bill(invoice2)
        }  just Runs

        // when
        billingJob.runOnce()

        // then
        verify { billingService.bill(invoice1) }
        verify { billingService.bill(invoice2) }
    }

    private fun anInvoice(status: InvoiceStatus) : Invoice {
        return Invoice(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD),
            status,
            LocalDate.now())
    }

}