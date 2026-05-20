package com.compose.chi.domain.result

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val error: DomainError) : Resource<Nothing>
}
