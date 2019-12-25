/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.PocketService
import io.pleo.antaeus.data.AntaeusConnectionProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.DatabaseHelper
import io.pleo.antaeus.job.AntaeusJobRunner
import io.pleo.antaeus.job.BillingJob
import io.pleo.antaeus.rest.AntaeusRest
import setupInitialData

fun main() {
    val db = DatabaseHelper.db("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")

    // Set up data access layer.
    val connectionProvider = AntaeusConnectionProvider(db)
    val dal = AntaeusDal(connectionProvider)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    // Get third parties
    val paymentProvider = getPaymentProvider()

    // Create core services
    val invoiceService = InvoiceService(dal = dal)
    val customerService = CustomerService(dal = dal)
    val pocketService = PocketService(dal = dal)

    // This is _your_ billing service to be included where you see fit
    val billingService = BillingService(
        connectionProvider = connectionProvider,
        paymentProvider = paymentProvider,
        invoiceService = invoiceService)

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService,
        pocketService = pocketService
    ).run()

    val jobs = listOf(
        BillingJob(
            invoiceService = invoiceService,
            billingService = billingService
        )
    )

    // Create Job service
    AntaeusJobRunner(
        jobs = jobs
    ).run()
}

