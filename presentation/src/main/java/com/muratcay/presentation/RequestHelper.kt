package com.muratcay.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

fun <T> flowCall(request: suspend () -> T?): Flow<Result<T>> {
    return flow<Result<T>> {
        emit(Result.Success(request.invoke()))
    }.onStart { Result.Loading }.catch {
        emit(Result.Error(it as? Exception))
    }
}