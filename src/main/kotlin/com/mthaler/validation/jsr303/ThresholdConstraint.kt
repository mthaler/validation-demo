package com.mthaler.validation.jsr303

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.validation.Constraint
import kotlin.reflect.KClass
import javax.validation.Payload

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = [])
annotation class ThresholdConstraint(

    val message: String = "{ThresholdConstraint.message}",

    val groups: Array<KClass<*>> = [],

    val payload: Array<KClass<out Payload>> = []
)