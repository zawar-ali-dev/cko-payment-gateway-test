package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Service responsible for communicating with the external bank simulator.
 */
@Service
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);

  private final RestClient restClient;

  public BankClient(RestClient bankRestClient) {
    this.restClient = bankRestClient;
  }

  public BankPaymentResponse authorizePayment(BankPaymentRequest request) {
    return restClient.post()
        .uri("/payments")
        .body(request)
        .retrieve()
        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
          LOG.error("Bank service unavailable with status {}", res.getStatusCode());
          throw new BankCommunicationException(
              "Bank service is currently unavailable. Please try again later.");
        })
        .body(BankPaymentResponse.class);
  }
}
