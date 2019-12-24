package io.pleo.antaeus.core.exceptions

abstract class EntityNotFoundException(entity: String, id: Int) : ApplicationException("$entity '$id' was not found")