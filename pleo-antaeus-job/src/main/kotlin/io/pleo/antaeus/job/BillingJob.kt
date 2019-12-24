package io.pleo.antaeus.job

import io.pleo.antaeus.core.services.BillingService
import mu.KotlinLogging
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

private val logger = KotlinLogging.logger {}

class BillingJob(private val billingService: BillingService): AntaeusJob() {

    override fun run() {
        fixedRateTimer(
            javaClass.name,
            daemon = true,
            period = Duration.ofMinutes(30).toMillis()) {
                logger.info("Start billing charge...")
                try {
                    runOnce()
                } catch (ex: RuntimeException) {
                    logger.error("Job failed due to", ex)
                }
                logger.info("Finish billing charge")
            }
    }

    fun runOnce() {
        billingService.chargeAll()
    }

}