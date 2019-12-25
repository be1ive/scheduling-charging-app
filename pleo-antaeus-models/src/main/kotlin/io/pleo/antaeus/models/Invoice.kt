package io.pleo.antaeus.models

import java.time.Instant
import java.time.LocalDate

data class Invoice(
    val id: Int,
    val customerId: Int,
    val amount: Money,
    val status: InvoiceStatus,
    val date: LocalDate,
    val paidAt: Instant? = null
) {

    fun isIn(status: InvoiceStatus): Boolean {
        return this.status == status
    }

    fun isPending(): Boolean {
        return isIn(InvoiceStatus.PENDING)
    }

    fun isPaid(): Boolean {
        return isIn(InvoiceStatus.PAID)
    }

    fun process() : Invoice {
        check(status == InvoiceStatus.PENDING) {
            "Invoice should be in PENDING state"
        }
        return copy(status = InvoiceStatus.PROCESSING)
    }

    fun pay() : Invoice {
        check(status == InvoiceStatus.PENDING || status == InvoiceStatus.PROCESSING) {
            "Invoice should be in PENDING or PROCESSING state"
        }
        return copy(status = InvoiceStatus.PAID, paidAt = Instant.now())
    }

    fun cancel(): Invoice {
        check(status == InvoiceStatus.PENDING || status == InvoiceStatus.PROCESSING) {
            "Invoice should be in PENDING or PROCESSING state"
        }
        return copy(status = InvoiceStatus.CANCELLED)
    }

}
