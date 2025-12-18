package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the response from the bank simulator.
 *
 * @param authorized true if the payment was authorized, false otherwise
 * @param authorizationCode the authorization code returned by the bank if authorized
 */
public record BankPaymentResponse(
    boolean authorized,
    @JsonProperty("authorization_code")
    String authorizationCode
) {}
