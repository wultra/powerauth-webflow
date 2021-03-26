/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.app.nextstep.exception;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.ExtendedError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.Violation;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    /**
     * Default exception handler, for unexpected errors.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        logger.error("Error occurred in Next Step server", t);
        Error error = new Error(Error.Code.ERROR_GENERIC, "Unknown error occurred, cause: " + t.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for HTTP message not readable exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(InvalidRequestException.CODE, "Invalid request data, cause: " + ex.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation already finished error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFinishedException(OperationAlreadyFinishedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationAlreadyFinishedException.CODE, "Operation is already in DONE state.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation already failed error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFailedException(OperationAlreadyFailedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationAlreadyFailedException.CODE, "Operation is already in FAILED state.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation already canceled error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyCanceledException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationCanceledException(OperationAlreadyCanceledException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationAlreadyCanceledException.CODE, "Operation update attempted for CANCELED operation.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationNotFoundException(OperationNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationNotFoundException.CODE, "Operation not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation is not valid.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationNotValidException(OperationNotValidException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationNotValidException.CODE, "Operation is not valid.");
        return new ErrorResponse(error);
    }
    /**
     * Exception handler for operation already exist error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyExistsException(OperationAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationAlreadyExistsException.CODE, "Operation already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for organization not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OrganizationNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOrganizationNotFoundException(OrganizationNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OrganizationNotFoundException.CODE, "Organization not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for organization already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OrganizationAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOrganizationAlreadyExistsException(OrganizationAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OrganizationNotFoundException.CODE, "Organization already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for step definition not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(StepDefinitionNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleStepDefinitionNotFoundException(StepDefinitionNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(StepDefinitionNotFoundException.CODE, "Step definition not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for step definition already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(StepDefinitionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleStepDefinitionAlreadyExistsException(StepDefinitionAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(StepDefinitionAlreadyExistsException.CODE, "Step definition already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for invalid operation data error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidOperationDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidOperationDataException(InvalidOperationDataException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(InvalidOperationDataException.CODE, "Operation contains invalid data.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for invalid request error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidRequestException(InvalidRequestException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(InvalidRequestException.CODE, "Request data is invalid, cause: " + ex.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for invalid configuration error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidConfigurationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidConfigurationException(InvalidConfigurationException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(InvalidConfigurationException.CODE, "Next Step configuration is invalid, cause: " + ex.getMessage());
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for authentication method not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(AuthMethodNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleAuthMethodNotFoundException(AuthMethodNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(AuthMethodNotFoundException.CODE, "Authentication method not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation config already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationConfigAlreadyExists.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationConfigAlreadyExists(OperationConfigAlreadyExists ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationConfigAlreadyExists.CODE, "Operation config already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation config not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationConfigNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationConfigNotFoundException(OperationConfigNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationConfigNotFoundException.CODE, "Operation config not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation and authentication method config already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationMethodConfigAlreadyExists.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationMethodConfigAlreadyExists(OperationMethodConfigAlreadyExists ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationMethodConfigAlreadyExists.CODE, "Operation and authentication config already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for operation and authentication method config not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationMethodConfigNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationMethodConfigNotFoundException(OperationMethodConfigNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OperationMethodConfigNotFoundException.CODE, "Operation and authentication config not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for authentication method already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(AuthMethodAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleAuthMethodAlreadyExistsException(AuthMethodAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(AuthMethodAlreadyExistsException.CODE, "Authentication method already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for application not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(ApplicationNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(ApplicationNotFoundException.CODE, "Application not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for application already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(ApplicationAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleApplicationAlreadyExistsException(ApplicationAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(ApplicationAlreadyExistsException.CODE, "Application already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for hashing configuration already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(HashConfigAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleHashingConfigAlreadyExistsException(HashConfigAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(HashConfigAlreadyExistsException.CODE, "Hashing configuration already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for hashing configuration is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(HashConfigNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleHashingConfigNotFoundException(HashConfigNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(HashConfigNotFoundException.CODE, "Hashing configuration not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for role already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(RoleAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleRoleAlreadyExistsException(RoleAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(RoleAlreadyExistsException.CODE, "Role already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for role is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleRoleNotFoundException(RoleNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(RoleNotFoundException.CODE, "Role not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for deleted not allowed exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(DeleteNotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleDeleteNotAllowedException(DeleteNotAllowedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(DeleteNotAllowedException.CODE, "Delete action is not allowed.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user role already assigned error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserRoleAlreadyAssignedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserRoleAlreadyAssignedException(UserRoleAlreadyAssignedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserRoleAlreadyAssignedException.CODE, "User role is already assigned.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for role is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserRoleNotAssignedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserRoleNotAssignedException(UserRoleNotAssignedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserRoleNotAssignedException.CODE, "User role is not assigned.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential policy already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialPolicyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialPolicyAlreadyExistsException(CredentialPolicyAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialPolicyAlreadyExistsException.CODE, "Credential policy already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential policy is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialPolicyNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialPolicyNotFoundException(CredentialPolicyNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialPolicyNotFoundException.CODE, "Credential policy not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for one time password policy already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpPolicyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpPolicyAlreadyExistsException(OtpPolicyAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpPolicyAlreadyExistsException.CODE, "One time password policy already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for one time password policy is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpPolicyNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpPolicyNotFoundException(OtpPolicyNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpPolicyNotFoundException.CODE, "One time password policy not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential definition already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialDefinitionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialDefinitionAlreadyExistsException(CredentialDefinitionAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialDefinitionAlreadyExistsException.CODE, "Credential definition already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential definition is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialDefinitionNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialDefinitionNotFoundException(CredentialDefinitionNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialDefinitionNotFoundException.CODE, "Credential definition not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for one time password definition already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpDefinitionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpDefinitionAlreadyExistsException(OtpDefinitionAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpDefinitionAlreadyExistsException.CODE, "One time password definition already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for one time password definition is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpDefinitionNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpDefinitionNotFoundException(OtpDefinitionNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpDefinitionNotFoundException.CODE, "One time password definition not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user identity already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserAlreadyExistsException.CODE, "User identity already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user identity not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserNotFoundException.CODE, "User identity not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user identity is not active.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserNotActiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserNotActiveException(UserNotActiveException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserNotActiveException.CODE, "User identity is not active.");
        return new ErrorResponse(error);
    }
    /**
     * Exception handler for user alias already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserAliasAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserAliasAlreadyExistsException(UserAliasAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserAliasAlreadyExistsException.CODE, "User alias already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user alias not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserAliasNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserAliasNotFoundException(UserAliasNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserAliasNotFoundException.CODE, "User alias not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user contact already exists error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserContactAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserContactAlreadyExistsException(UserContactAlreadyExistsException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserContactAlreadyExistsException.CODE, "User contact already exists.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user contact not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserContactNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserContactNotFoundException(UserContactNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserContactNotFoundException.CODE, "User contact not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for user identity not blocked error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(UserNotBlockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUserNotBlockedException(UserNotBlockedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(UserNotBlockedException.CODE, "User identity is not blocked.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialNotFoundException(CredentialNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialNotFoundException.CODE, "Credential not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential not active error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialNotActiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialNotActiveException(CredentialNotActiveException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialNotActiveException.CODE, "Credential is not active.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential not blocked error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialNotBlockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialNotBlockedException(CredentialNotBlockedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(CredentialNotBlockedException.CODE, "Credential is not blocked.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for OTP generation algorithm not supported.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpGenAlgorithmNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpGenAlgorithmNotSupportedException(OtpGenAlgorithmNotSupportedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpGenAlgorithmNotSupportedException.CODE, "One time password generation algorithm is not supported.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for one time password is not found error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OtpNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOtpNotFoundException(OtpNotFoundException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(OtpNotFoundException.CODE, "One time password not found.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for case when encryption or decryption fails.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(EncryptionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialDecryptionFailed(EncryptionException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        Error error = new Error(EncryptionException.CODE, "Encryption or decryption failed.");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for credential validation failed error.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(CredentialValidationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleCredentialValidationFailedException(CredentialValidationFailedException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        logger.warn("Validation errors: {}", ex.getError().getValidationFailures());
        CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE, "Credential validation failed.", ex.getError().getValidationFailures());
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for failed request validations.
     *
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.warn("Error occurred in Next Step server: {}", ex.getMessage());
        final ExtendedError error = new ExtendedError(RequestValidationFailedException.CODE, "Request validation failed.");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.getViolations().add(
                    new Violation(fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage())
            );
        }
        return new ErrorResponse(error);
    }

}
