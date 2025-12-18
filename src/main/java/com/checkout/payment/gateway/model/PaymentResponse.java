package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.List;
import java.util.UUID;

/**
 * Represents the response sent to the merchant after processing a payment request.
 *
 * @param id UUID of the payment
 * @param status the status of the payment (AUTHORIZED, DECLINED, REJECTED)
 * @param cardNumberLastFour last four digits of the card
 * @param expiryMonth expiry month of the card
 * @param expiryYear expiry year of the card
 * @param currency payment currency
 * @param amount payment amount
 * @param errors list of validation errors, if any
 */
public record PaymentResponse(
    UUID id,
    PaymentStatus status,
    String cardNumberLastFour,
    Integer expiryMonth,
    Integer expiryYear,
    String currency,
    Integer amount,
    List<String> errors
) {}
