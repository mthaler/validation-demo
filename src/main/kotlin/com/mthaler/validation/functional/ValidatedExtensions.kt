package com.mthaler.validation.functional

import arrow.core.Validated

fun <E, A, B>Validated<List<E>, A>.andThen(f: (A) -> Validated<List<E>, B>): Validated<List<E>, B> = when(this) {
    is Validated.Invalid -> this
    is Validated.Valid -> f(this.value)
}