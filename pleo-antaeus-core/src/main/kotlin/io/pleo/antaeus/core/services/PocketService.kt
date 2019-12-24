/*
    Implements endpoints related to pockets.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.PocketCurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.PocketNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money
import io.pleo.antaeus.models.Pocket
import java.time.LocalDate

class PocketService(private val dal: AntaeusDal) {

    fun fetchBasePocketFor(customerId: Int): Pocket {
        val customer = dal.fetchCustomer(customerId)
            ?: throw CustomerNotFoundException(customerId)
        return dal.fetchBasePocketFor(customer)
            ?: throw PocketCurrencyMismatchException(customer.id, customer.baseCurrency)
    }

    fun fetch(id: Int): Pocket {
        return dal.fetchPocket(id) ?: throw PocketNotFoundException(id)
    }

    fun create(currency: Currency, customerId: Int): Pocket {
        val customer = dal.fetchCustomer(customerId)
            ?: throw CustomerNotFoundException(customerId)
        return dal.createPocket(currency, customer)
    }

    fun update(pocket: Pocket): Pocket {
        return dal.updatePocket(pocket)
    }
}
