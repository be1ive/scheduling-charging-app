package io.pleo.antaeus.job

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class AntaeusJobRunner(private val jobs: Collection<AntaeusJob> ) {

    fun run() {
        GlobalScope.launch {
            jobs.forEach {
                it.run()
            }
            while (true) {}

        }
    }
}