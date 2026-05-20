package com.compose.chi.domain.result

sealed interface DomainError {
    data object Network : DomainError
    data object NotFound : DomainError
    data object Server : DomainError
    data object Unknown : DomainError
}
