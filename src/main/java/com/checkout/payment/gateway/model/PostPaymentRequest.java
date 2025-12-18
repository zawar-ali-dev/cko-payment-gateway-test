package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * DTO representing a payment request from the merchant to the Payment Gateway.
 *
 * @param cardNumber card number to charge
 * @param expiryMonth month of card expiry
 * @param expiryYear year of card expiry
 * @param currency currency of the payment
 * @param amount payment amount in minor units (e.g., cents)
 * @param cvv card verification value
 */
public record PostPaymentRequest(

    @JsonProperty("card_number")
    String cardNumber,

    @JsonProperty("expiry_month")
    Integer expiryMonth,

    @JsonProperty("expiry_year")
    Integer expiryYear,

    String currency,
    Integer amount,
    String cvv
) {

  /**
   * Returns the expiry date in MM/YYYY format.
   */
  @JsonProperty("expiry_date")
  public String expiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }

  public String cardNumberLastFour() {
    return cardNumber.substring(cardNumber.length() - 4);
  }
}
