package io.pleo.antaeus.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class PocketTest {

    @Test
    fun `will throw if debited in wrong currency`() {
        // given
        val aPocket = Pocket(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD))

        // when / then
        assertThrows<IllegalStateException> {
            aPocket.debit(Money(1_00, Currency.GBP))
        }
    }

    @Test
    fun `will throw if debited over balance`() {
        // given
        val aPocket = Pocket(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD))

        // when / then
        assertThrows<IllegalStateException> {
            aPocket.debit(Money(100_50, Currency.USD))
        }
    }

    @Test
    fun `will be successfully debited`() {
        // given
        val aPocket = Pocket(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD))

        // when
        val updated = aPocket.debit(Money(1_00, Currency.USD))

        // then
        assertAll("pocket",
            { assertEquals(Money(99_00, Currency.USD), updated.balance) }
        )
    }

    @Test
    fun `will throw if credited in wrong currency`() {
        // given
        val aPocket = Pocket(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD))

        // when / then
        assertThrows<IllegalStateException> {
            aPocket.credit(Money(1_00, Currency.GBP))
        }
    }

    @Test
    fun `will be successfully credited`() {
        // given
        val aPocket = Pocket(
            Random.nextInt(),
            Random.nextInt(),
            Money(100_00, Currency.USD))

        // when
        val updated = aPocket.credit(Money(1_00, Currency.USD))

        // then
        assertAll("pocket",
            { assertEquals(Money(101_00, Currency.USD), updated.balance) }
        )
    }
}