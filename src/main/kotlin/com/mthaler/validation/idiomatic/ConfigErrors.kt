package com.mthaler.validation.idiomatic

class ConfigErrors private constructor() {

    internal interface ConfigError

    internal class CouldNotParse : ConfigError

    internal class ParameterIsMissing(val parameterName: String) : ConfigError {
        override fun toString(): String {
            return "Parameter '$parameterName' is missing"
        }
    }

    internal class NoBootstrapServers : ConfigError {
        override fun toString(): String {
            return "Bootstrap servers must be a non empty list of valid hosts and port"
        }
    }

    internal class InvalidHost(val incorrectValue: String, val positionInArray: Int) : ConfigError {
        override fun toString(): String {
            return "'$incorrectValue'is not a valid host in 'bootstrapServers' at index $positionInArray"
        }
    }

    internal class ThresholdATooLow(val incorrectValue: Int, val minAllowedValue: Int) : ConfigError {
        override fun toString(): String {
            return "thresholdA : $incorrectValue is not above $minAllowedValue"
        }
    }

    internal class ThresholdCTooHigh(val incorrectValue: Int, val maxAllowedValue: Int) : ConfigError {
        override fun toString(): String {
            return "thresholdC: " + incorrectValue + "is not under " + maxAllowedValue
        }
    }

    internal class ThresholdBNotInBetween(val incorrectValue: Int, val suppliedA: Int, val suppliedC: Int) :
        ConfigError {
        override fun toString(): String {
            return "thresholdB must be between thresholdA and thresholdC: " + incorrectValue + "is not between " + suppliedA + " and " + suppliedC
        }
    }
}