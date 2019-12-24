package io.pleo.antaeus.models

data class Pocket(
    val id: Int,
    val customerId: Int,
    val balance: Money
) {
    fun debit(amount: Money): Pocket {
        check(amount.currency == balance.currency) { "Amount currency should be same as pocket currency" }
        check(amount <= balance) { "Amount should be less then or equals to pocket balance" }
        return copy(balance = balance - amount)
    }

    fun credit(amount: Money): Pocket {
        check(amount.currency == balance.currency) { "Amount currency should be same as pocket currency" }
        return copy(balance = balance + amount)
    }

}
