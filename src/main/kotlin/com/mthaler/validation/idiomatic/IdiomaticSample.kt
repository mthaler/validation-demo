package com.mthaler.validation.idiomatic

import com.typesafe.config.ConfigFactory
import com.mthaler.validation.idiomatic.ConfigErrors.ConfigError
import com.typesafe.config.ConfigException.Missing
import com.mthaler.validation.idiomatic.ConfigErrors.ParameterIsMissing
import com.typesafe.config.ConfigException.WrongType
import com.google.common.net.HostAndPort
import com.mthaler.validation.idiomatic.ConfigErrors.NoBootstrapServers
import java.lang.IllegalArgumentException
import com.mthaler.validation.idiomatic.ConfigErrors.InvalidHost
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdATooLow
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdCTooHigh
import com.mthaler.validation.idiomatic.ConfigErrors.ThresholdBNotInBetween
import org.slf4j.LoggerFactory
import java.util.ArrayList

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger("IdiomaticSample")
    val config = ConfigFactory.load()
    val errors: MutableList<ConfigError> = ArrayList()
    val rawBootstrapServers: List<String?>? = try {
        config.getStringList("kafka.bootstrapServers")
    } catch (ex: Missing) {
        errors.add(ParameterIsMissing("kafka.bootstrapServers"))
        null
    } catch (ex: WrongType) {
        errors.add(ParameterIsMissing("kafka.bootstrapServers"))
        null
    }
    val applicationId: String? = try {
        config.getString("kafka.applicationId")
    } catch (ex: Missing) {
        errors.add(ParameterIsMissing("kafka.applicationId"))
        null
    } catch (ex: WrongType) {
        errors.add(ParameterIsMissing("kafka.applicationId"))
        null
    }
    val tA: Int? = try {
        config.getInt("app.thresholdA")
    } catch (ex: Missing) {
        errors.add(ParameterIsMissing("app.thresholdA"))
        null
    } catch (ex: WrongType) {
        errors.add(ParameterIsMissing("app.thresholdA"))
        null
    }
    val tB: Int? = try {
        config.getInt("app.thresholdB")
    } catch (ex: Missing) {
        errors.add(ParameterIsMissing("app.thresholdB"))
        null
    } catch (ex: WrongType) {
        errors.add(ParameterIsMissing("app.thresholdB"))
        null
    }
    val tC: Int? = try {
        config.getInt("app.thresholdC")
    } catch (ex: Missing) {
        errors.add(ParameterIsMissing("app.thresholdC"))
        null
    } catch (ex: WrongType) {
        errors.add(ParameterIsMissing("app.thresholdC"))
        null
    }
    val bootstrapServers: MutableList<HostAndPort> = ArrayList()
    if (rawBootstrapServers!!.isEmpty()) {
        errors.add(NoBootstrapServers())
    } else {
        var rawBootstrapServer: String?
        for (i in rawBootstrapServers.indices) {
            rawBootstrapServer = rawBootstrapServers[i]
            try {
                bootstrapServers.add(HostAndPort.fromString(rawBootstrapServer!!).withDefaultPort(9092))
            } catch (e: IllegalArgumentException) {
                errors.add(InvalidHost(rawBootstrapServer!!, i))
            }
        }
    }

    // tA
    if (tA != null && tA < 0) {
        errors.add(ThresholdATooLow(tA, 0))
    }
    if (tC != null && tC > 10000) {
        errors.add(ThresholdCTooHigh(tC, 10000))
    }
    if (tA != null && tB != null && tC != null && (tB > tC || tB < tA)) {
        errors.add(ThresholdBNotInBetween(tB, tA, tC))
    }
    if (errors.isEmpty()) {
        log.info("All good")
    } else {
        for (error in errors) {
            log.error(error.toString())
        }
    }
}