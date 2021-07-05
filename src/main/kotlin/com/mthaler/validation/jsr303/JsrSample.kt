package com.mthaler.validation.jsr303

import kotlin.jvm.JvmStatic
import javax.validation.Validation
import com.typesafe.config.ConfigFactory
import com.google.common.net.HostAndPort
import java.lang.IllegalArgumentException
import org.slf4j.LoggerFactory
import java.util.ArrayList

object JsrSample {
    @JvmStatic
    fun main(args: Array<String>) {
        val log = LoggerFactory.getLogger("JsrSample")
        log.info("Starting sample for JSR 303")
        val factory = Validation.buildDefaultValidatorFactory()
        val validator = factory.validator


        // Extracting all the config
        val config = ConfigFactory.load()
        val rawBootstrapServers = config.getStringList("kafka.bootstrapServers")
        val applicationId = config.getString("kafka.applicationId")
        val tA = config.getInt("app.thresholdA")
        val tB = config.getInt("app.thresholdB")
        val tC = config.getInt("app.thresholdC")

        // We need to do the HostAndPort validation by hand.
        val bootstrapServers: MutableList<HostAndPort> = ArrayList()
        val invalidValidHosts: MutableList<String> = ArrayList()
        for (rawBootstrapServer in rawBootstrapServers) {
            try {
                bootstrapServers.add(HostAndPort.fromString(rawBootstrapServer).withDefaultPort(9092))
            } catch (e: IllegalArgumentException) {
                invalidValidHosts.add(rawBootstrapServer)
            }
        }
        val kafkaConfig = KafkaConfig(bootstrapServers, applicationId)
        val businessConfig = BusinessConfig(tA, tB, tC)
        val wholeConfig = WholeConfig(businessConfig, kafkaConfig)
        val constraintViolations = validator.validate(businessConfig)

        // Displaying errors.
        for (violation in constraintViolations) {
            log.info("Constraint violation {}", violation)
        }
        if (constraintViolations.isEmpty()) {
            log.info("All good")
        }
    }
}