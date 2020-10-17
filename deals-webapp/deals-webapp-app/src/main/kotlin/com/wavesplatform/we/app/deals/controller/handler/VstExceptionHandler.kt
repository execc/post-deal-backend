package com.wavesplatform.we.app.deals.controller.handler

import com.wavesplatform.vst.contract.factory.exception.ContractPreValidationException
import com.wavesplatform.we.app.deals.api.exception.ErrorDto
import com.wavesplatform.we.app.deals.api.exception.VstErrorCode.FORBIDDEN
import com.wavesplatform.we.app.deals.api.exception.VstErrorCode.HTTP_MESSAGE_NOT_READABLE
import com.wavesplatform.we.app.deals.api.exception.VstErrorCode.INTERNAL_SERVER_ERROR
import com.wavesplatform.we.app.deals.api.exception.VstErrorCode.METHOD_ARGUMENT_NOT_VALID
import com.wavesplatform.we.app.deals.api.exception.VstRestApiException
import java.util.ArrayList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

const val HTTP_MESSAGE_NOT_READABLE_TEXT = "Ошибка валидации самого Http сообщения"
const val FORBIDDEN_TEXT = "Доступ запрещен"

@RestControllerAdvice
class VstExceptionHandler {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(VstRestApiException::class)
    fun handle(restApiException: VstRestApiException) = ResponseEntity
        .status(restApiException.httpStatus.value())
        .body(restApiException.errorDto)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<ErrorDto> {
        val errors = ArrayList<String>()
        for (error in ex.bindingResult.fieldErrors) {
            errors.add(error.defaultMessage!! + ": " + error.field)
        }
        val errorDto = ErrorDto(METHOD_ARGUMENT_NOT_VALID.value, errors[0])
        return ResponseEntity(errorDto, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handle(ex: HttpMessageNotReadableException): ResponseEntity<ErrorDto> {
        val errorDto = ErrorDto(
                errorCode = HTTP_MESSAGE_NOT_READABLE.value,
                message = HTTP_MESSAGE_NOT_READABLE_TEXT + "; " + ex.message
        )
        log.debug("Error parsing JSON", ex)
        return ResponseEntity(errorDto, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handle(ex: AccessDeniedException): ResponseEntity<ErrorDto> = ResponseEntity
        .status(HttpStatus.FORBIDDEN).body(
                    ErrorDto(
                            errorCode = FORBIDDEN.value,
                            message = ex.message ?: FORBIDDEN_TEXT
                    )
        ).also { log.debug("Forbidden", ex) }

    @ExceptionHandler(ContractPreValidationException::class)
    fun handle(ex: ContractPreValidationException) = ResponseEntity(
            ErrorDto(METHOD_ARGUMENT_NOT_VALID.value, ex.cause!!.localizedMessage),
            HttpStatus.BAD_REQUEST
    ).also { log.error("Bad contract request!", ex) }

    @ExceptionHandler(Exception::class)
    fun handle(ex: Exception) = ResponseEntity(
            ErrorDto(INTERNAL_SERVER_ERROR.value, ex.localizedMessage),
        HttpStatus.INTERNAL_SERVER_ERROR
    ).also { log.error("Uncaught exception!", ex) }
}
