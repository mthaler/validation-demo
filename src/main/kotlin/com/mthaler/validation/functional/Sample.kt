package com.mthaler.validation.functional

import arrow.core.*
import arrow.typeclasses.Monoid
import com.google.common.net.HostAndPort
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

/**
 *
 */

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger("KotlinArrowSample")

    val config = ConfigFactory.load()

    val businessValidation = validateBusinessConfig(config)
    val kafkaValidation = validateKafkaConfig(config)

    val finalValidation = kafkaValidation.zip(businessValidation) { a, b ->
        ApplicationConfig(a, b)
    }


    val toLog = finalValidation.fold({errors ->
        "Invalid : " + errors
    }, { conf ->
        "Valid: $conf"
    })

    log.info(toLog)

}

fun <A> get(path: String, extractor: (String) -> A): ValidatedNel<ConfigError, A> = try {
    Valid(extractor(path))
} catch (e: ConfigException.Missing) {
    ConfigError.ParameterIsMissing(path).invalidNel()
} catch (e: ConfigException.WrongType) {
    ConfigError.CouldNotParse.invalidNel()
}

fun validateBusinessConfig(config: Config): ValidatedNel<ConfigError, BusinessConfig> = run {
    // Validated does not have a flatMap method, thus we need to use Either
    val unvalidatedTAE = get("app.thresholdA", { p -> config.getInt(p)}).toEither()
    val tAe = unvalidatedTAE.flatMap { unvalidatedTa ->
        if (unvalidatedTa < 0 )
            Either.Left(nonEmptyListOf(ConfigError.ThresholdATooLow(unvalidatedTa, 0)))
        else
            Either.Right(unvalidatedTa)
    }.toValidated()

    val unvalidatedTCE = get("app.thresholdC", { p -> config.getInt(p)}).toEither()
    val tCe = unvalidatedTCE.flatMap { unvalidatedTc ->
        if (unvalidatedTc > 10000)
            Either.Left(nonEmptyListOf(ConfigError.ThresholdCTooHigh(unvalidatedTc, 10000)))
        else
            Either.Right(unvalidatedTc)
    }.toValidated()

    val unvalidatedTBE = get("app.thresholdB", {p -> config.getInt(p)}).toEither()

    val tBe = unvalidatedTAE.flatMap { ta -> unvalidatedTBE.flatMap { tb -> unvalidatedTCE.flatMap { tc ->
        if (ta < tb && tb < tc)
            Either.Right(tb)
        else
            Either.Left(nonEmptyListOf(ConfigError.ThresholdBNotInBetween(tb, ta, tc)))
    } } }.toValidated()


    tAe.zip(tBe, tCe) { a, b, c ->
        BusinessConfig(a, b, c)
    }
}

fun validateKafkaConfig(config: Config): ValidatedNel<ConfigError, KafkaConfig> = run {

    val applicationIdV = get("kafka.applicationId", { config.getString(it) })

    val serversE: ValidatedNel<ConfigError, List<String>> = get("kafka.bootstrapServers", {config.getStringList(it)})

    val serversV: ValidatedNel<ConfigError, List<HostAndPort>> = serversE.map { rawList ->
        if (rawList.isEmpty()){
            ConfigError.NoBootstrapServers.invalidNel()
        } else {
            rawList.withIndex().map { validateHost(it.value, it.index) }.traverseValidated { it }
        }.fold(Monoid.list())
    }

    applicationIdV.zip(serversV) { applicationID, servers ->
        KafkaConfig(applicationID, servers)
    }
}

fun validateHost(rawString: String, index: Int): ValidatedNel<ConfigError, HostAndPort> = try {
    Valid(HostAndPort.fromString(rawString).withDefaultPort(9092))
} catch (e: IllegalArgumentException) {
    ConfigError.InvalidHost(rawString, index).invalidNel()
}