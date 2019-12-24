package io.pleo.antaeus.job

import io.mockk.*
import io.pleo.antaeus.core.services.BillingService
import org.junit.jupiter.api.Test

class BillingJobTest {

    private val billingService = mockk<BillingService>()

    private val billingJob = BillingJob(billingService)

    @Test
    fun `will successfully call billing service`() {
        // given
        every {
            billingService.chargeAll()
        } just Runs

        // when
        billingJob.runOnce()

        // then
        verify { billingService.chargeAll() }
    }


}