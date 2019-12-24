package io.pleo.antaeus.data

abstract class AbstractDal(private val connectionProvider: ConnectionProvider) {

    fun <T> inTransaction(block: () -> T): T {
        return connectionProvider.inTransaction {
            block()
        }
    }

}