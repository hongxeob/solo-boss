package com.soloboss.ai.web.v1.common

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
    val message: String,
)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFound(ex: EntityNotFoundException): ErrorResponse = ErrorResponse(message = ex.message ?: "리소스를 찾을 수 없습니다.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException): ErrorResponse {
        val message =
            ex.bindingResult.fieldErrors
                .firstOrNull()
                ?.defaultMessage ?: "요청 값이 올바르지 않습니다."
        return ErrorResponse(message = message)
    }
}
