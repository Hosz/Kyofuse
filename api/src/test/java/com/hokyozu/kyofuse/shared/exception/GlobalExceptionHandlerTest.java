package com.hokyozu.kyofuse.shared.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundReturnsNotFoundResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(
                new NotFoundException("Comentário não encontrado.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Comentário não encontrado.");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleConflictReturnsConflictResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleConflict(new ConflictException("Email já está em uso."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Email já está em uso.");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleUnauthorizedReturnsUnauthorizedResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleUnauthorized(new UnauthorizedException("Credenciais inválidas."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("Credenciais inválidas.");
    }

    @Test
    void handleForbiddenReturnsForbiddenResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleForbidden(
                new ForbiddenException("Você não tem permissão para acessar este recurso.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
        assertThat(response.getBody().message()).isEqualTo("Você não tem permissão para acessar este recurso.");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleBadRequestReturnsBadRequestResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleUnauthorized(new BadRequestException("Invalid request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
    }

    @Test
    void handleConstraintViolationReturnsBadRequestResponse() {
        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(
                new ConstraintViolationException("getFeed.page: must be greater than or equal to 0", java.util.Set.of())
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("getFeed.page: must be greater than or equal to 0");
    }

    @Test
    void handleHandlerMethodValidationReturnsBadRequestResponse() throws NoSuchMethodException {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyValidationTarget", String.class);
        ParameterValidationResult parameterResult = new ParameterValidationResult(
                new MethodParameter(method, 0),
                -1,
                java.util.List.of(new DefaultMessageSourceResolvable(
                        new String[]{"Min"},
                        "must be greater than or equal to 0"
                )),
                null,
                null,
                null,
                (resolvable, type) -> null
        );
        HandlerMethodValidationException exception = new HandlerMethodValidationException(
                MethodValidationResult.create(this, method, java.util.List.of(parameterResult))
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleHandlerMethodValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isNotBlank();
    }

    @Test
    void handleValidationReturnsFirstMessagePerField() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Email inválido."));
        bindingResult.addError(new FieldError("request", "email", "Email obrigatório."));
        bindingResult.addError(new FieldError("request", "username", "Username obrigatório."));
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyValidationTarget", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ValidationErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Dados inválidos.");
        assertThat(response.getBody().fields())
                .containsEntry("email", "Email inválido.")
                .containsEntry("username", "Username obrigatório.");
    }

    @Test
    void handleHttpMessageNotReadableReturnsEnumFieldMessageWhenInvalidEnumHasPath() {
        InvalidFormatException invalidFormatException = InvalidFormatException.from(
                null,
                "Invalid enum",
                "TEAM",
                PostVisibility.class
        );
        invalidFormatException.prependPath(new JsonMappingException.Reference(Object.class, "visibility"));
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Could not read JSON",
                new RuntimeException(invalidFormatException),
                null
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid value for field: visibility");
    }

    @Test
    void handleHttpMessageNotReadableUsesDefaultFieldWhenInvalidEnumHasNoPath() {
        InvalidFormatException invalidFormatException = InvalidFormatException.from(
                null,
                "Invalid enum",
                "TEAM",
                PostVisibility.class
        );
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Could not read JSON",
                invalidFormatException,
                null
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid value for field: field");
    }

    @Test
    void handleHttpMessageNotReadableReturnsGenericMessageWhenCauseIsNotInvalidEnum() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Could not read JSON",
                new IllegalArgumentException("Malformed JSON"),
                null
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request body");
    }

    @Test
    void handleHttpMessageNotReadableReturnsGenericMessageWhenInvalidFormatIsNotEnum() {
        InvalidFormatException invalidFormatException = InvalidFormatException.from(
                null,
                "Invalid string",
                123,
                String.class
        );
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Could not read JSON",
                invalidFormatException,
                null
        );

        ResponseEntity<ApiErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request body");
    }

    @SuppressWarnings("unused")
    private void dummyValidationTarget(String request) {
    }
}
