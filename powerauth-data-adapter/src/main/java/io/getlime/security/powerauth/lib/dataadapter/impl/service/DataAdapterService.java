package io.getlime.security.powerauth.lib.dataadapter.impl.service;

import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.lib.dataadapter.configuration.DataAdapterConfiguration;
import io.getlime.security.powerauth.lib.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.impl.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.impl.validation.CreateSMSAuthorizationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.*;
import io.getlime.security.powerauth.lib.dataadapter.model.request.*;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.dataadapter.repository.SMSAuthorizationRepository;
import io.getlime.security.powerauth.lib.dataadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountAttribute;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample implementation of DataAdapter interface which should be updated in real implementation.
 *
 * @author Roman Strobl
 */
@Service
public class DataAdapterService implements DataAdapter {

    private final SMSAuthorizationRepository smsAuthorizationRepository;
    private final DataAdapterConfiguration dataAdapterConfiguration;
    private final OperationFormDataService operationFormDataService;
    private final CreateSMSAuthorizationRequestValidator requestValidator;

    public DataAdapterService(SMSAuthorizationRepository smsAuthorizationRepository, DataAdapterConfiguration dataAdapterConfiguration,
                              OperationFormDataService operationFormDataService, CreateSMSAuthorizationRequestValidator requestValidator) {
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.dataAdapterConfiguration = dataAdapterConfiguration;
        this.operationFormDataService = operationFormDataService;
        this.requestValidator = requestValidator;
    }

    @Override
    public AuthenticationResponse authenticateUser(AuthenticationRequest authenticationRequest) throws AuthenticationFailedException, MethodArgumentNotValidException {
        // input validation is handled by AuthenticationRequestValidator
        // validation is invoked manually because of the generified Request object
        AuthenticationRequestValidator validator = new AuthenticationRequestValidator();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(authenticationRequest, "authenticationRequest");
        ValidationUtils.invokeValidator(validator, authenticationRequest, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object(){}.getClass().getEnclosingMethod(),0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }

        // here will be the real authentication - call to the backend providing authentication
        if ("test".equals(authenticationRequest.getPassword())) {
            return new AuthenticationResponse(authenticationRequest.getUsername());
        } else {
            throw new AuthenticationFailedException("login.authenticationFailed");
        }
    }

    @Override
    public UserDetailResponse fetchUserDetail(UserDetailRequest userDetailRequest) throws MethodArgumentNotValidException {
        String userId = userDetailRequest.getId();

        // Fetch user details here ...
        // In case user is not found, throw a new MethodArgumentNotValidException().

        UserDetailResponse responseObject = new UserDetailResponse();
        responseObject.setId(userId);
        responseObject.setGivenName("John");
        responseObject.setFamilyName("Doe");
        return responseObject;
    }

    @Override
    public BankAccountListResponse fetchBankAccounts(BankAccountListRequest bankAccountListRequest) throws MethodArgumentNotValidException {
        String userId = bankAccountListRequest.getUserId();

        // Fetch bank account list for given user here from the bank backend.
        // In case user is not found, throw a new MethodArgumentNotValidException().
        BankAccountListResponse responseObject = new BankAccountListResponse();
        responseObject.setUserId(userId);

        // Replace mock bank account data with real data loaded from the bank backend.
        // In case the bank account selection is disabled, return an empty list.
        List<BankAccount> bankAccounts = new ArrayList<>();

        BankAccount bankAccount1 = new BankAccount();
        bankAccount1.setName("Běžný účet v CZK");
        bankAccount1.setBalance(new BigDecimal("24394.52"));
        bankAccount1.setNumber("12345678/1234");
        bankAccount1.setCurrency("CZK");
        bankAccount1.setUsableForPayment(true);
        bankAccounts.add(bankAccount1);

        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setName("Spořící účet v CZK");
        bankAccount2.setBalance(new BigDecimal("158121.10"));
        bankAccount2.setNumber("87654321/4321");
        bankAccount2.setCurrency("CZK");
        bankAccount2.setUsableForPayment(true);
        bankAccounts.add(bankAccount2);

        BankAccount bankAccount3 = new BankAccount();
        bankAccount3.setName("Spořící účet v EUR");
        bankAccount3.setBalance(new BigDecimal("1.90"));
        bankAccount3.setNumber("44444444/1111");
        bankAccount3.setCurrency("EUR");
        bankAccount3.setUsableForPayment(false);
        bankAccount3.setUnusableForPaymentReason("operationReview.balanceTooLow");
        bankAccounts.add(bankAccount3);

        responseObject.setBankAccounts(bankAccounts);
        return responseObject;
    }

    @Override
    public void formDataChangedNotification(FormDataChangeNotificationRequest notificationRequest) {
        FormDataChange change = notificationRequest.getFormDataChange();
        switch (change.getType()) {
            case BANK_ACCOUNT_CHOICE:
                bankAccountChosen((BankAccountChoice) change, notificationRequest.getUserId(), notificationRequest.getOperationId());
                break;
            case AUTH_METHOD_CHOICE:
                authMethodChosen((AuthMethodChoice) change, notificationRequest.getUserId(), notificationRequest.getOperationId());
                break;
            default:
                throw new IllegalStateException("Invalid change entity type: " + change.getType());
        }
    }

    private void bankAccountChosen(BankAccountChoice bankAccountChoice, String userId, String operationId) {
        // Handle bank account choice here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Bank account chosen: " + bankAccountChoice.getBankAccountNumber() + ", user: " + userId + ", operationId: " + operationId);
    }

    private void authMethodChosen(AuthMethodChoice authMethodChoice, String userId, String operationId) {
        // Handle authorization method choice here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Authorization method chosen: " + authMethodChoice.getChosenAuthMethod() + ", user: " + userId + ", operationId: " + operationId);
    }

    @Override
    public void operationChangedNotification(OperationChangeNotificationRequest notificationRequest) {
        String operationId = notificationRequest.getOperationId();
        String userId = notificationRequest.getUserId();
        OperationChange change = notificationRequest.getOperationChange();
        // Handle operation change here (e.g. send notification to bank backend).
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Operation changed: " + change + ", user: " + userId + ", operationId: " + operationId);
    }

    @Override
    public CreateSMSAuthorizationResponse createAuthorizationSMS(CreateSMSAuthorizationRequest createSMSRequest) throws MethodArgumentNotValidException, SMSAuthorizationFailedException {
        // input validation is handled by CreateSMSAuthorizationRequestValidator
        // validation is invoked manually because of the generified Request object
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(createSMSRequest, "createSMSAuthorizationRequest");
        ValidationUtils.invokeValidator(requestValidator, createSMSRequest, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object() {
            }.getClass().getEnclosingMethod(), 0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }

        // messageId is generated as random UUID, it can be overridden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // update names of operationData JSON fields if necessary
        OperationAmountAttribute amountAttribute = operationFormDataService.getAmount(createSMSRequest.getOperationFormData());
        BigDecimal amount = amountAttribute.getAmount();
        String currency = amountAttribute.getCurrency();
        String account = operationFormDataService.getAccount(createSMSRequest.getOperationFormData());

        // update localized SMS message text in resources
        final DataDigest.Result digestResult = generateAuthorizationCode(amount, currency, account);
        final String authorizationCode = digestResult.getDigest();
        final byte[] salt = digestResult.getSalt();
        String[] messageArgs = {amount.toPlainString(), currency, account, authorizationCode};
        String messageText = messageSource().getMessage("sms-otp.text", messageArgs, new Locale(createSMSRequest.getLang()));

        SMSAuthorizationEntity smsEntity = new SMSAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setOperationId(createSMSRequest.getOperationId());
        smsEntity.setUserId(createSMSRequest.getUserId());
        smsEntity.setOperationName(createSMSRequest.getOperationName());
        smsEntity.setAuthorizationCode(authorizationCode);
        smsEntity.setSalt(salt);
        smsEntity.setMessageText(messageText);
        smsEntity.setVerifyRequestCount(0);
        smsEntity.setTimestampCreated(new Date());
        smsEntity.setTimestampExpires(new DateTime().plusSeconds(dataAdapterConfiguration.getSmsOtpExpirationTime()).toDate());
        smsEntity.setTimestampVerified(null);
        smsEntity.setVerified(false);

        // store entity in database
        smsAuthorizationRepository.save(smsEntity);

        // Add here code to send the SMS OTP message to user identified by userId with messageText.
        // In case message delivery fails, throw new SMSAuthorizationFailedException()

        return new CreateSMSAuthorizationResponse(messageId);
    }

    @Override
    public void verifyAuthorizationSMS(VerifySMSAuthorizationRequest verifySMSRequest) throws SMSAuthorizationFailedException {
        String messageId = verifySMSRequest.getMessageId();
        SMSAuthorizationEntity smsEntity = smsAuthorizationRepository.findOne(messageId);
        if (smsEntity == null) {
            throw new SMSAuthorizationFailedException("smsAuthorization.invalidMessage");
        }
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.invalidCode");
        }
        if (smsEntity.isExpired()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.expired");
        }
        if (smsEntity.isVerified()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.alreadyVerified");
        }
        if (smsEntity.getVerifyRequestCount() > dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.maxAttemptsExceeded");
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        String authorizationCodeActual = verifySMSRequest.getAuthorizationCode();
        if (!authorizationCodeActual.equals(authorizationCodeExpected)) {
            throw new SMSAuthorizationFailedException("smsAuthorization.failed");
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);
    }

    /**
     * Authorization code generation - to be updated based on application requirements.
     *
     * @return Generated authorization code.
     */
    private DataDigest.Result generateAuthorizationCode(BigDecimal amount, String currency, String account) {
        List<String> digestItems = new ArrayList<>();
        digestItems.add(amount.toPlainString());
        digestItems.add(currency);
        digestItems.add(account);
        return new DataDigest().generateDigest(digestItems);
    }

    /**
     * Get MessageSource with i18n data for authorizations SMS messages.
     *
     * @return MessageSource.
     */
    @Bean
    private MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/static/resources/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
