package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.client.RestClient.ResponseSpec;

@ExtendWith(MockitoExtension.class)
class BankClientTest {

  @Mock
  private RestClient restClient;

  @Mock
  private RequestBodyUriSpec requestBodyUriSpec;

  @Mock
  private RequestBodySpec requestBodySpec;

  @Mock
  private ResponseSpec responseSpec;

  private BankClient bankClient;

  @BeforeEach
  void setUp() {
    bankClient = new BankClient(restClient);
  }

  @Test
  void authorizePayment_whenBankReturnsAuthorized_shouldReturnAuthorizedResponse() {
    BankPaymentRequest request = new BankPaymentRequest(
        "2222405343248111",
        "04/2025",
        "GBP",
        100,
        "123"
    );

    BankPaymentResponse expectedResponse = new BankPaymentResponse(
        true,
        "0bb07405-6d44-4b50-a14f-7ae0beff13ad"
    );

    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/payments")).thenReturn(requestBodySpec);
    when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(BankPaymentResponse.class)).thenReturn(expectedResponse);

    BankPaymentResponse response = bankClient.authorizePayment(request);

    assertNotNull(response);
    assertTrue(response.authorized());
    assertEquals("0bb07405-6d44-4b50-a14f-7ae0beff13ad", response.authorizationCode());
  }

  @Test
  void authorizePayment_whenBankReturnsDeclined_shouldReturnDeclinedResponse() {
    BankPaymentRequest request = new BankPaymentRequest(
        "2222405343248888",
        "04/2025",
        "GBP",
        100,
        "123"
    );

    BankPaymentResponse expectedResponse = new BankPaymentResponse(false, null);

    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/payments")).thenReturn(requestBodySpec);
    when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.body(BankPaymentResponse.class)).thenReturn(expectedResponse);

    BankPaymentResponse response = bankClient.authorizePayment(request);

    assertNotNull(response);
    assertFalse(response.authorized());
  }

  @Test
  void authorizePayment_whenBankReturns503_shouldThrowBankCommunicationException() {
    BankPaymentRequest request = new BankPaymentRequest(
        "2222405343248880",
        "04/2025",
        "GBP",
        100,
        "123"
    );

    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri("/payments")).thenReturn(requestBodySpec);
    when(requestBodySpec.body(request)).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenThrow(
        new BankCommunicationException("Bank service is currently unavailable. Please try again later.")
    );

    BankCommunicationException exception = assertThrows(
        BankCommunicationException.class,
        () -> bankClient.authorizePayment(request)
    );

    assertEquals("Bank service is currently unavailable. Please try again later.",
        exception.getMessage());
  }
}