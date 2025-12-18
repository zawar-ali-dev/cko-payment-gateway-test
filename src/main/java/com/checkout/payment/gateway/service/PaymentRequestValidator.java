package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Validator for {@link PostPaymentRequest}.
 * <p>
 * Handles card, CVV, amount, currency, and expiry date validation.
 */
@Component
public class PaymentRequestValidator {

  private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "GBP", "EUR");
  private static final int MIN_CARD_LENGTH = 14;
  private static final int MAX_CARD_LENGTH = 19;
  private static final int MIN_CVV_LENGTH = 3;
  private static final int MAX_CVV_LENGTH = 4;

  public List<String> validate(PostPaymentRequest request) {
    List<String> errors = new ArrayList<>();

    // Card number
    String card = request.cardNumber();
    if (card == null || card.isBlank()) {
      errors.add("Card number is required");
    } else {
      if (!card.matches("\\d+")) errors.add("Card number must contain only numeric characters");
      if (card.length() < MIN_CARD_LENGTH || card.length() > MAX_CARD_LENGTH)
        errors.add("Card number must be between 14-19 characters");
    }

    // Expiry date
    int month = request.expiryMonth();
    int year = request.expiryYear();
    if (month < 1 || month > 12) {
      errors.add("Expiry month must be between 1-12");
    } else if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
      errors.add("Card expiry date must be in the future");
    }


    // Currency
    String currency = request.currency();
    if (currency == null || currency.isBlank()) {
      errors.add("Currency is required");
    } else if (currency.length() != 3 || !SUPPORTED_CURRENCIES.contains(currency.toUpperCase())) {
      errors.add("Currency must be one of: " + SUPPORTED_CURRENCIES);
    }

    // Amount
    int amount = request.amount();
    if (amount <= 0) errors.add("Amount must be greater than zero");

    // CVV
    String cvv = request.cvv();
    if (cvv == null || cvv.isBlank()) {
      errors.add("CVV is required");
    } else if (!cvv.matches("\\d{" + MIN_CVV_LENGTH + "," + MAX_CVV_LENGTH + "}")) {
      errors.add("CVV must be 3-4 digits");
    }

    return errors;
  }
}
