package io.pleo.antaeus.core.services.specs

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate

class BillingServiceSpec: AbstractServiceSpec() {

    private val paymentService = mockk<PaymentProvider>()
    private val invoiceService = InvoiceService(dal)
    private val billingService = BillingService(connectionProvider, paymentService, invoiceService)

    @Test
    fun `will update pending invoice and create next`() {
        // given
        val customer = dal.createCustomer(Currency.USD)
        val pendingInvoice = dal.createInvoice(
            Money(100, Currency.USD),
            LocalDate.now(),
            customer,
            InvoiceStatus.PENDING)
        val cancelledInvoice = dal.createInvoice(
            Money(200, Currency.USD),
            LocalDate.now(),
            customer,
            InvoiceStatus.CANCELLED)

        every {
            paymentService.charge(any())
        } returns true

        // when
        billingService.chargeAll()

        // then pending invoice updated
        val updatedInvoice = dal.fetchInvoice(pendingInvoice.id)!!
        assertAll("pending invoice",
            { Assertions.assertEquals(InvoiceStatus.PAID, updatedInvoice.status) },
            { Assertions.assertNotNull(updatedInvoice.paidAt) })

        // and canceled invoice not updated
        val notUpdatedInvoice = dal.fetchInvoice(cancelledInvoice.id)!!
        assertAll("canceled invoice",
            { Assertions.assertEquals(InvoiceStatus.CANCELLED, notUpdatedInvoice.status) },
            { Assertions.assertNull(notUpdatedInvoice.paidAt) })

        // and new invoice created
        val newInvoice = dal.fetchUnpaidInvoicesAt(LocalDate.now().plusMonths(1)).firstOrNull()
        assertAll("new invoice",
            { Assertions.assertNotNull(newInvoice) },
            { Assertions.assertEquals(InvoiceStatus.PENDING, newInvoice?.status) })

    }

}