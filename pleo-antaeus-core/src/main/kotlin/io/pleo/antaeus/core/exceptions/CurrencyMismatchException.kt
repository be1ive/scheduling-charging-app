package io.pleo.antaeus.core.exceptions

class CurrencyMismatchException(invoiceId: Int, customerId: Int) :
    ApplicationException("Currency of invoice '$invoiceId' does not match currency of customer '$customerId'")
