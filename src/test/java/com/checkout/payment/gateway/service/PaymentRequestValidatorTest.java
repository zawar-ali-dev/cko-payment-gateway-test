package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentRequestValidatorTest {

  private PaymentRequestValidator validator;

  @BeforeEach
  void setUp() {
    validator = new PaymentRequestValidator();
  }

  @Test
  void validate_whenAllFieldsValid_shouldReturnEmptyErrors() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.isEmpty(), "Expected no validation errors");
  }

  @Test
  void validate_whenCardNumberMissing_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertEquals(1, errors.size());
    assertEquals("Card number is required", errors.get(0));
  }

  @Test
  void validate_whenCardNumberInvalidCharacters_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111abcd1111",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.contains("Card number must contain only numeric characters"));
  }

  @Test
  void validate_whenCardNumberTooShortOrTooLong_shouldReturnError() {
    PostPaymentRequest shortCard = new PostPaymentRequest(
        "123",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    PostPaymentRequest longCard = new PostPaymentRequest(
        "1234567890123456789012345",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    List<String> shortErrors = validator.validate(shortCard);
    List<String> longErrors = validator.validate(longCard);

    assertTrue(shortErrors.contains("Card number must be between 14-19 characters"));
    assertTrue(longErrors.contains("Card number must be between 14-19 characters"));
  }

  @Test
  void validate_whenExpiryMonthInvalid_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        0,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.contains("Expiry month must be between 1-12"));
  }

  @Test
  void validate_whenExpiryDateInPast_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        1,
        2000,
        "USD",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.contains("Card expiry date must be in the future"));
  }

  @Test
  void validate_whenCurrencyInvalid_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        YearMonth.now().getYear() + 1,
        "XYZ",
        1000,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.get(0).startsWith("Currency must be one of"));
  }

  @Test
  void validate_whenAmountInvalid_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        0,
        "123"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.contains("Amount must be greater than zero"));
  }

  @Test
  void validate_whenCvvInvalid_shouldReturnError() {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        YearMonth.now().getYear() + 1,
        "USD",
        1000,
        "12"
    );

    List<String> errors = validator.validate(request);

    assertTrue(errors.contains("CVV must be 3-4 digits"));
  }
}
