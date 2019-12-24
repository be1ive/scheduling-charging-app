package io.pleo.antaeus.core.exceptions

import io.pleo.antaeus.models.Currency

class PocketCurrencyMismatchException(customerId: Int, currency: Currency)
    : ApplicationException("Pocket in '$currency' was not found for '$customerId'")