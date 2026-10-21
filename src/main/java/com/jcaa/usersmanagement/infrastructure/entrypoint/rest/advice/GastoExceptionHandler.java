package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.advice;

import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoAmountException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoDateException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIdException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaConsistencyException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoIvaRateException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoOwnerException;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoPlaceException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce las excepciones del dominio Gasto a codigos HTTP.
 *
 * <p>La distincion importante es entre 400 y 422: un dato malformado es
 * sintaxis incorrecta (400), mientras que unos totales que no cuadran son
 * sintacticamente validos pero semanticamente imposibles, y eso es
 * exactamente lo que significa 422 Unprocessable Entity.
 */
@RestControllerAdvice
public class GastoExceptionHandler {

  @ExceptionHandler(GastoNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiErrorResponse handleNotFound(final GastoNotFoundException exception) {
    return new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage());
  }

  @ExceptionHandler(InvalidGastoIvaConsistencyException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public ApiErrorResponse handleFiscalInvariant(
      final InvalidGastoIvaConsistencyException exception) {
    return new ApiErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getMessage());
  }

  @ExceptionHandler({
    InvalidGastoAmountException.class,
    InvalidGastoDateException.class,
    InvalidGastoIdException.class,
    InvalidGastoIvaRateException.class,
    InvalidGastoOwnerException.class,
    InvalidGastoPlaceException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleInvalidInput(final RuntimeException exception) {
    return new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
  }
}
