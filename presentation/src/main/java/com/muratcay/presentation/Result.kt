package com.muratcay.presentation

sealed class Result<out T>(
    val data: T? = null,
    val error: Exception? = null
) {

    class Success<out T>(data: T?) : Result<T>(data)
    object Loading : Result<Nothing>()
    class Error<T>(error: Exception? = null) : Result<T>(error = error)
}