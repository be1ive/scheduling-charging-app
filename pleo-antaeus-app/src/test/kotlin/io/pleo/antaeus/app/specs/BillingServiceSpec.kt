package io.pleo.antaeus.app.specs

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerInsufficientFundsException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
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

        every {
            paymentService.charge(any())
        } returns true

        // when
        billingService.bill(pendingInvoice)

        // then customer charged
        verify(exactly = 1) {
            paymentService.charge(any())
        }

        // and pending invoice paid
        val updatedInvoice = dal.fetchInvoice(pendingInvoice.id)!!
        assertAll("pending invoice",
            { Assertions.assertEquals(InvoiceStatus.PAID, updatedInvoice.status) },
            { Assertions.assertNotNull(updatedInvoice.paidAt) })

        // and new invoice created
        val newInvoice = dal.fetchUnpaidInvoicesAt(LocalDate.now().plusMonths(1)).firstOrNull()
        assertAll("new invoice",
            { Assertions.assertNotNull(newInvoice) },
            { Assertions.assertEquals(InvoiceStatus.PENDING, newInvoice?.status) })

    }

    @Test
    fun `will not update pending invoice and create next`() {
        // given
        val customer = dal.createCustomer(Currency.USD)
        val pendingInvoice = dal.createInvoice(
            Money(100, Currency.USD),
            LocalDate.now(),
            customer,
            InvoiceStatus.PENDING)

        every {
            paymentService.charge(any())
        } throws CustomerInsufficientFundsException(pendingInvoice.customerId)

        // when
        assertThrows<CustomerInsufficientFundsException> {
            billingService.bill(pendingInvoice)
        }

        // then customer charged
        verify(exactly = 1) {
            paymentService.charge(any())
        }

        // and pending invoice not changed
        val updatedInvoice = dal.fetchInvoice(pendingInvoice.id)!!
        assertAll("pending invoice",
            { Assertions.assertEquals(InvoiceStatus.PENDING, updatedInvoice.status) },
            { Assertions.assertNull(updatedInvoice.paidAt) })

    }

    @Test
    fun `will not charge customer multiple times if called concurrently`() {
        // given
        val customer = dal.createCustomer(Currency.USD)
        val pendingInvoices = (1..5).map {
            dal.createInvoice(
                Money(100, Currency.USD),
                LocalDate.now(),
                customer,
                InvoiceStatus.PENDING)
        }.toList()

        every {
            paymentService.charge(any())
        } returns true

        // when
        val runs = (1..25).map {
            GlobalScope.async(start = CoroutineStart.LAZY) {
                try {
                    pendingInvoices.shuffled().forEach {
                        billingService.bill(it)
                    }
                } catch (e: Exception) {}
            }
        }
        runBlocking {
            runs.forEach { it.start() }
            runs.forEach { it.await() }
        }

        // then
        verify(exactly = 5) {
            paymentService.charge(any())
        }
    }

}