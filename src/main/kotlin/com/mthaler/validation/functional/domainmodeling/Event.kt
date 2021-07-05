package com.mthaler.validation.functional.domainmodeling

import arrow.core.*
import java.time.LocalDate

data class ValidationError(val reason: String)

inline class EventId(val value: Long) {
    companion object {
        fun create(value: Long): ValidatedNel<ValidationError, EventId> =
            if (value > 0) Valid(EventId(value))
            else ValidationError("EventId needs to be bigger than 0, but found $value.").invalidNel()
    }
}

inline class Organizer(val value: String) {
    companion object {
        fun create(value: String): ValidatedNel<ValidationError, Organizer> = when {
            value.isEmpty() -> ValidationError("Organizer cannot be empty").invalidNel()
            value.isBlank() -> ValidationError("Organizer cannot be blank").invalidNel()
            else -> Valid(Organizer(value))
        }
    }
}

inline class Title(val value: String) {
    companion object {
        fun create(value: String): ValidatedNel<ValidationError, Title> = when {
            value.isEmpty() -> ValidationError("Organizer cannot be empty").invalidNel()
            value.isBlank() -> ValidationError("Organizer cannot be blank").invalidNel()
            else -> Valid(Title(value))
        }
    }
}
inline class Description(val value: String) {
    companion object {
        fun create(value: String): ValidatedNel<ValidationError, Description> = when {
            value.isEmpty() -> ValidationError("Organizer cannot be empty").invalidNel()
            value.isBlank() -> ValidationError("Organizer cannot be blank").invalidNel()
            else -> Valid(Description(value))
        }
    }
}

data class Event(
    val id: EventId,
    val title: Title,
    val organizer: Organizer,
    val description: Description,
    val date: LocalDate
)

suspend fun generateId(): Long =
    -1L

suspend fun date(): LocalDate =
    LocalDate.now()

suspend fun createEvent(): ValidatedNel<ValidationError, Event> =
    EventId.create(generateId()).zip(
        Title.create(""),
        Organizer.create(""),
        Description.create("")
    ) { id, title, organizer, description -> Event(id, title, organizer, description, date()) }