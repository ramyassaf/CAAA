package com.compose.chi.data.remote

/**
 * Network-layer configuration constants for the `:data` module.
 *
 * Kept `internal` so the rest of the app can't accidentally reach in and
 * read transport-level details — anything outside `:data` should go
 * through repository contracts in `:domain`.
 */
internal const val BASE_URL_JOKES: String = "https://official-joke-api.appspot.com"
