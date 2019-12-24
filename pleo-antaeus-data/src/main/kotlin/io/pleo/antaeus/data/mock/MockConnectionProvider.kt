package io.pleo.antaeus.data.mock

import io.pleo.antaeus.data.AntaeusConnectionProvider
import io.pleo.antaeus.data.ConnectionProvider

class MockConnectionProvider: ConnectionProvider() {

    override fun <T> inTransaction(block: () -> T): T {
        return block()
    }
}