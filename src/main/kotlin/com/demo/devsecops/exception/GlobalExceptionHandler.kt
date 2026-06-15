package com.demo.devsecops.exception

import com.demo.devsecops.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        exception: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        logger.warn(
            "Resource not found: path={}, status={}, message={}",
            request.requestURI,
            status.value(),
            exception.message
        )

        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = exception.message ?: "Resource not found",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(
        exception: DuplicateResourceException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        logger.warn(
            "Duplicate resource: path={}, status={}, message={}",
            request.requestURI,
            status.value(),
            exception.message
        )

        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = exception.message ?: "Resource already exists",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST

        val message = exception.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn(
            "Validation failed: path={}, status={}, message={}",
            request.requestURI,
            status.value(),
            message.ifBlank { "Validation failed" }
        )

        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message.ifBlank { "Validation failed" },
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        exception: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        logger.error(
            "Unexpected server error: path={}, status={}, message={}",
            request.requestURI,
            status.value(),
            exception.message,
            exception
        )

        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = exception.message ?: "Unexpected server error",
                path = request.requestURI
            )
        )
    }
}
