package io.pleo.antaeus.models

import java.math.BigDecimal

data class Money(
    val minorValue: Long,
    val currency: Currency
) {
    val value: BigDecimal get() = BigDecimal.valueOf(minorValue, currency.minorUnits)

    operator fun minus(money: Money): Money {
        check(currency == money.currency) { "Money currencies should be equals" }
        return copy(minorValue = minorValue - money.minorValue)
    }

    operator fun plus(money: Money): Money {
        check(currency == money.currency) { "Money currencies should be equals" }
        return copy(minorValue = minorValue + money.minorValue)
    }

    operator fun compareTo(money: Money): Int {
        check(currency == money.currency) { "Money currencies should be equals" }
        return minorValue.compareTo(money.minorValue)
    }
}
