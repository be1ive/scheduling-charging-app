package io.pleo.antaeus.core.services

import io.mockk.Called
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerInsufficientFundsException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceAlreadyChargedException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.mock.MockConnectionProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class BillingServiceTest {

    private val connectionProvider = MockConnectionProvider()
    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingService = BillingService(
        connectionProvider,
        paymentProvider,
        invoiceService)


    @BeforeEach
    internal fun setUp() {
        every {
            invoiceService.update(any())
        } answers {
            firstArg()
        }

        every {
            invoiceService.create(any(), any(), any())
        } returns anInvoice(InvoiceStatus.PENDING)

    }

    @Test
    fun `will pay invoice and charge customer if invoice is pending and customer was not charged`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns true

        // when
        billingService.bill(invoice)

        // then
        verify {
            paymentProvider.charge(invoice)
        }

        verify {
            invoiceService.create(
                invoice.amount,
                invoice.date
                    .withDayOfMonth(1)
                    .plusMonths(1),
                invoice.customerId)
        }

        val updated = mutableListOf<Invoice>()
        verify {
            invoiceService.update(capture(updated))
        }
        assertEquals(invoice.id, updated.last().id)
        assertEquals(InvoiceStatus.PAID, updated.last().status)
    }

    @Test
    fun `will not pay invoice and not charge customer if invoice is paid`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PAID)

        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns true

        // when
        billingService.bill(invoice)

        // then
        verify {
            paymentProvider wasNot Called
        }

        verify(exactly = 0) {
            invoiceService.create(any(), any(), any())
        }

        verify(exactly = 0) {
            invoiceService.update(any())
        }
    }

    @Test
    fun `will pay invoice and try charge customer if invoice is pending and customer was already charged`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } throws InvoiceAlreadyChargedException(invoice.id, invoice.customerId)

        // when
        billingService.bill(invoice)

        // then
        verify {
            paymentProvider.charge(invoice)
        }

        verify {
            invoiceService.create(
                invoice.amount,
                invoice.date
                    .withDayOfMonth(1)
                    .plusMonths(1),
                invoice.customerId)
        }

        val updated = mutableListOf<Invoice>()
        verify {
            invoiceService.update(capture(updated))
        }
        assertEquals(invoice.id, updated.last().id)
        assertEquals(InvoiceStatus.PAID, updated.last().status)
    }

    @Test
    fun `will throw if invoice is pending and customer has not enough funds`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns false

        // when / then
        assertThrows<CustomerInsufficientFundsException> {
            billingService.bill(invoice)
        }
    }

    @Test
    fun `will cancel invoice if invoice is pending and customer was not found`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } throws CustomerNotFoundException(invoice.customerId)

        // when
        billingService.bill(invoice)

        // then
        verify {
            paymentProvider.charge(invoice)
        }

        verify(exactly = 0) {
            invoiceService.create(any(), any(), any())
        }

        val updated = mutableListOf<Invoice>()
        verify {
            invoiceService.update(capture(updated))
        }
        assertEquals(invoice.id, updated.last().id)
        assertEquals(InvoiceStatus.CANCELLED, updated.last().status)
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