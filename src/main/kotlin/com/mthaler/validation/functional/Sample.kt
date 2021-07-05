package com.mthaler.validation.functional

import arrow.core.*
import arrow.typeclasses.Monoid
import arrow.typeclasses.Semigroup
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

//    val businessValidation = validateBusinessConfig(config)
//    val kafkaValidation = validateKafkaConfig(config)
//
//    val finalValidation: ValidatedNel<ConfigError, ApplicationConfig> = Validated.applicative<Nel<ConfigError>>(Nel.semigroup())
//        .map(kafkaValidation, businessValidation, { ApplicationConfig(it.a, it.b) }).fix()
//
//    val toLog = finalValidation.fold({errors ->
//        "Invalid : " + errors.show()
//    }, { conf ->
//        "Valid: $conf"
//    })
//
//    log.info(toLog)

}

fun <A> get(path: String, extractor: (String) -> A): ValidatedNel<ConfigError, A> = try {
    Valid(extractor(path))
} catch (e: ConfigException.Missing) {
    ConfigError.ParameterIsMissing(path).invalidNel()
} catch (e: ConfigException.WrongType) {
    ConfigError.CouldNotParse.invalidNel()
}

//fun validateBusinessConfig(config: Config): ValidatedNel<ConfigError, BusinessConfig> = run {
//    val unvalidatedTAE = get("app.thresholdA", { p -> config.getInt(p)})
//    val tAe = unvalidatedTAE.flatMap { unvalidatedTa ->
//        if (unvalidatedTa < 0 )
//            Either.Left(ConfigError.ThresholdATooLow(unvalidatedTa, 0))
//        else
//            Either.Right(unvalidatedTa)
//    }
//
//    val unvalidatedTCE = get("app.thresholdC", { p -> config.getInt(p)})
//    val tCe = unvalidatedTCE.flatMap { unvalidatedTc ->
//        if (unvalidatedTc > 10000)
//            Either.Left(ConfigError.ThresholdCTooHigh(unvalidatedTc, 10000))
//        else
//            Either.Right(unvalidatedTc)
//    }
//
//    val unvalidatedTBE = get("app.thresholdB", {p -> config.getInt(p)})
//
//    val tBe = EitherT.monad<ConfigError>().binding{
//        val ta = unvalidatedTAE.bind()
//        val tb = unvalidatedTBE.bind()
//        val tc = unvalidatedTCE.bind()
//        if (ta < tb && tb < tc)
//            Either.Right(tb).bind()
//        else
//            Either.Left(ConfigError.ThresholdBNotInBetween(tb, ta, tc)).bind()
//
//    }.fix()
//
//
//    val tAV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tAe).toValidatedNel()
//    val tBV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tBe).toValidatedNel()
//    val tCV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tCe).toValidatedNel()
//
//    ValidatedNel.applicative(Nel.semigroup<ConfigError>()).map(tAV, tBV, tCV, {
//        val a = it.a
//        val b = it.b
//        val c = it.c
//        BusinessConfig(a, b, c)
//    }).fix()
//}
//

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