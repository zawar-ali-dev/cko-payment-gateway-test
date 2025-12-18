package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a request sent to the bank simulator for payment authorization.
 *
 * @param cardNumber the card number to charge
 * @param expiryDate the card expiry date in MM/YYYY format
 * @param currency the currency of the payment (e.g., USD, EUR)
 * @param amount the payment amount in minor units (e.g., cents)
 * @param cvv the card verification value
 */
public record BankPaymentRequest(
    @JsonProperty("card_number")
    String cardNumber,

    @JsonProperty("expiry_date")
    String expiryDate, // MM/YYYY

    String currency,
    int amount,
    String cvv
) {}
