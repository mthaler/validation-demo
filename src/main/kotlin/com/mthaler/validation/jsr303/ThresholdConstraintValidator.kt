package com.mthaler.validation.jsr303

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ThresholdConstraintValidator : ConstraintValidator<ThresholdConstraint?, BusinessConfig?> {

    override fun isValid(value: BusinessConfig?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false
        val valid = value.thresholdA < value.thresholdB && value.thresholdB < value.thresholdC
        if (!valid) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("thresholdB should verify thresholdA < thresholdB < thresholdC")
                .addPropertyNode("thresholdB")
                .addConstraintViolation()
        }
        return valid
    }
}