package io.pleo.antaeus.core.exceptions

class InvoiceAlreadyChargedException(invoiceId: Int, customerId: Int) :
    ApplicationException("Invoice '$invoiceId' was already charged for '$customerId'")
