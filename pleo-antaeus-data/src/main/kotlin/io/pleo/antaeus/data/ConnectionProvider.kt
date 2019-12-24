package io.pleo.antaeus.data

abstract class ConnectionProvider {

    abstract fun <T> inTransaction(block: () -> T): T

}