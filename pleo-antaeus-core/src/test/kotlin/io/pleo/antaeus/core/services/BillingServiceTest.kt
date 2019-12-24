package io.pleo.antaeus.core.services

import io.mockk.*
import io.mockk.junit5.MockKExtension
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceAlreadyChargedException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.ConnectionProvider
import io.pleo.antaeus.data.mock.MockConnectionProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import net.bytebuddy.matcher.ElementMatchers.any
import org.junit.jupiter.api.Assertions
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
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)
        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns true

        // when
        billingService.chargeAll()

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
        assertEquals(invoice.id, updated.first().id)
    }

    @Test
    fun `will not pay invoice and not charge customer if invoice is paid`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PAID)

        every {
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)
        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns true

        // when
        billingService.chargeAll()

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
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)
        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } throws InvoiceAlreadyChargedException(invoice.id, invoice.customerId)

        // when
        billingService.chargeAll()

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
        assertEquals(invoice.id, updated.first().id)
    }

    @Test
    fun `will not pay invoice and try charge customer if invoice is pending and customer has not enough funds`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)
        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } returns false

        // when
        billingService.chargeAll()

        // then
        verify {
            paymentProvider.charge(invoice)
        }

        verify(exactly = 0) {
            invoiceService.create(any(), any(), any())
        }

        verify(exactly = 0) {
            invoiceService.update(any())
        }

    }

    @Test
    fun `will pay invoice and charge customer if invoice is pending and customer was not charged and network problem happened`() {
        // given
        val invoice = anInvoice(InvoiceStatus.PENDING)

        every {
            invoiceService.fetchUnpaidInvoicesAt(any())
        } returns listOf(invoice)
        every {
            invoiceService.fetch(invoice.id)
        } returns invoice
        every {
            paymentProvider.charge(invoice)
        } throws NetworkException() andThen true

        // when
        billingService.chargeAll()

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
        assertEquals(invoice.id, updated.first().id)
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