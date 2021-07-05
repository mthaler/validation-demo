package com.mthaler.validation.idiomatic

import kotlin.jvm.JvmStatic
import com.typesafe.config.ConfigFactory
import com.mthaler.validation.idiomatic.ConfigErrors.ConfigError
import com.google.common.net.HostAndPort
import com.mthaler.validation.idiomatic.ConfigErrors.NoBootstrapServers
import java.lang.IllegalArgumentException
import com.mthaler.validation.idiomatic.ConfigErrors.InvalidHost
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdATooLow
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdCTooHigh
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdBNotInBetween
import com.typesafe.config.ConfigException.Missing
import com.mthaler.validation.idiomatic.ConfigErrors.ParameterIsMissing
import com.typesafe.config.ConfigException.WrongType
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Supplier

object Idiomatic2Sample {

    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("IdiomaticSample")
        val config = ConfigFactory.load()
        val errors: MutableList<ConfigError> = ArrayList()
        val rawBootstrapServersOpt =
            get("kafka.bootstrapServers", errors) { config.getStringList("kafka.bootstrapServers") }
        val applicationId = get("kafka.applicationId", errors) { config.getString("kafka.applicationId") }
        val tA = get("app.thresholdA", errors) { config.getInt("app.thresholdA") }
        val tB = get("app.thresholdB", errors) { config.getInt("app.thresholdB") }
        val tC = get("app.thresholdC", errors) { config.getInt("app.thresholdC") }
        val bootstrapServers: MutableList<HostAndPort> = ArrayList()
        rawBootstrapServersOpt.ifPresent { rawBootstrapServers: List<String> ->
            if (rawBootstrapServers.isEmpty()) {
                errors.add(NoBootstrapServers())
            } else {
                for (i in rawBootstrapServers.indices) {
                    val rawBootstrapServer = rawBootstrapServers[i]
                    try {
                        bootstrapServers.add(HostAndPort.fromString(rawBootstrapServer).withDefaultPort(9092))
                    } catch (e: IllegalArgumentException) {
                        errors.add(InvalidHost(rawBootstrapServer, i))
                    }
                }
            }
        }


        // tA
        tA.ifPresent { p: Int ->
            if (p < 0) {
                errors.add(ThresholdATooLow(p, 0))
            }
        }
        tC.ifPresent { c: Int ->
            if (c > 10000) {
                errors.add(ThresholdCTooHigh(c, 10000))
            }
        }
        tA.ifPresent { a: Int ->
            tC.ifPresent { c: Int ->
                tB.ifPresent { b: Int ->
                    if (b > c || b < a) {
                        errors.add(ThresholdBNotInBetween(b, a, c))
                    }
                }
            }
        }
        if (errors.isEmpty()) {
            log.info("All good")
        } else {
            for (error in errors) {
                log.error(error.toString())
            }
        }
    }

    operator fun <A> get(path: String, errors: MutableList<ConfigError>, extractor: Supplier<A>): Optional<A> {
        return try {
            Optional.ofNullable(extractor.get())
        } catch (ex: Missing) {
            errors.add(ParameterIsMissing(path))
            Optional.empty()
        } catch (ex: WrongType) {
            errors.add(ParameterIsMissing(path))
            Optional.empty()
        }
    }
}