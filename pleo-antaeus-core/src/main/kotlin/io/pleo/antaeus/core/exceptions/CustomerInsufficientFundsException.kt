package io.pleo.antaeus.core.exceptions

class CustomerInsufficientFundsException(customerId: Int) : ApplicationException("Customer '$customerId' has insufficient funds")