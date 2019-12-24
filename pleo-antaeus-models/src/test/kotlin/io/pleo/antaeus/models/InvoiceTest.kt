package io.pleo.antaeus.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random

class InvoiceTest {

    @Test
    fun `will throw if try to pay in wrong state`() {
        // given
        val anInvoice = Invoice(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD),
            InvoiceStatus.CANCELLED,
            LocalDate.now())

        // when / then
        assertThrows<IllegalStateException> {
            anInvoice.pay()
        }
    }

    @Test
    fun `will be successfully paid`() {
        // given
        val anInvoice = Invoice(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD),
            InvoiceStatus.PENDING,
            LocalDate.now())

        // when
        val updated = anInvoice.pay()

        // then
        assertAll("invoice",
            { assertEquals(InvoiceStatus.PAID, updated.status) },
            { assertNotNull(updated.paidAt) }
        )
    }

    @Test
    fun `will throw if try to cancel in wrong state`() {
        // given
        val anInvoice = Invoice(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD),
            InvoiceStatus.PAID,
            LocalDate.now(),
            Instant.now())

        // when / then
        assertThrows<IllegalStateException> {
            anInvoice.cancel()
        }
    }

    @Test
    fun `will be successfully cancelled`() {
        // given
        val anInvoice = Invoice(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD),
            InvoiceStatus.PENDING,
            LocalDate.now())

        // when
        val updated = anInvoice.cancel()

        // then
        assertAll("invoice",
            { assertEquals(InvoiceStatus.CANCELLED, updated.status) }
        )
    }

}