package com.checkout.payment.gateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  @Mock
  private PaymentRequestValidator paymentRequestValidator;

  private PaymentGatewayService paymentGatewayService;

  @BeforeEach
  void setUp() {
    paymentGatewayService = new PaymentGatewayService(
        paymentsRepository,
        bankClient,
        paymentRequestValidator
    );
  }

  @Test
  void getPaymentById_whenPaymentExists_shouldReturnPayment() {
    UUID paymentId = UUID.randomUUID();
    PaymentResponse expectedPayment = new PaymentResponse(
        paymentId,
        PaymentStatus.AUTHORIZED,
        "1111",
        4,
        2025,
        "GBP",
        100,
        null
    );

    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(expectedPayment));

    Optional<PaymentResponse> result = paymentGatewayService.getPaymentById(paymentId);

    assertTrue(result.isPresent());
    assertEquals(expectedPayment, result.get());
    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void getPaymentById_whenPaymentDoesNotExist_shouldReturnEmpty() {
    UUID paymentId = UUID.randomUUID();
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.empty());

    Optional<PaymentResponse> result = paymentGatewayService.getPaymentById(paymentId);

    assertTrue(result.isEmpty());
    verify(paymentsRepository).get(paymentId);
  }

  @Test
  void processPayment_whenValidAndBankAuthorizes_shouldReturnAuthorizedPayment() {
    PostPaymentRequest request = new PostPaymentRequest(
        "2222405343248111",
        4,
        2025,
        "GBP",
        100,
        "123"
    );

    when(paymentRequestValidator.validate(request)).thenReturn(Collections.emptyList());
    when(bankClient.authorizePayment(any(BankPaymentRequest.class)))
        .thenReturn(new BankPaymentResponse(true, "0bb07405-6d44-4b50-a14f-7ae0beff13ad"));

    PaymentResponse response = paymentGatewayService.processPayment(request);

    assertNotNull(response);
    assertNotNull(response.id());
    assertEquals(PaymentStatus.AUTHORIZED, response.status());
    assertEquals("8111", response.cardNumberLastFour());
    assertEquals(4, response.expiryMonth());
    assertEquals(2025, response.expiryYear());
    assertEquals("GBP", response.currency());
    assertEquals(100, response.amount());
    assertNull(response.errors());

    verify(paymentRequestValidator).validate(request);
    verify(bankClient).authorizePayment(any(BankPaymentRequest.class));
    verify(paymentsRepository).add(any(PaymentResponse.class));
  }

  @Test
  void processPayment_whenValidAndBankDeclines_shouldReturnDeclinedPayment() {
    PostPaymentRequest request = new PostPaymentRequest(
        "2222405343248888",
        4,
        2025,
        "GBP",
        100,
        "123"
    );

    when(paymentRequestValidator.validate(request)).thenReturn(Collections.emptyList());
    when(bankClient.authorizePayment(any(BankPaymentRequest.class)))
        .thenReturn(new BankPaymentResponse(false, null));

    PaymentResponse response = paymentGatewayService.processPayment(request);

    assertNotNull(response);
    assertNotNull(response.id());
    assertEquals(PaymentStatus.DECLINED, response.status());
    assertEquals("8888", response.cardNumberLastFour());
    assertNull(response.errors());

    verify(paymentRequestValidator).validate(request);
    verify(bankClient).authorizePayment(any(BankPaymentRequest.class));
    verify(paymentsRepository).add(any(PaymentResponse.class));
  }

  @Test
  void processPayment_whenValidationFails_shouldReturnRejectedPayment() {
    PostPaymentRequest request = new PostPaymentRequest(
        "123",
        4,
        2025,
        "GBP",
        100,
        "123"
    );

    List<String> validationErrors = List.of(
        "Card number must be between 14-19 characters long"
    );

    when(paymentRequestValidator.validate(request)).thenReturn(validationErrors);

    PaymentResponse response = paymentGatewayService.processPayment(request);

    assertNotNull(response);
    assertNull(response.id());
    assertEquals(PaymentStatus.REJECTED, response.status());
    assertEquals(validationErrors, response.errors());

    verify(paymentRequestValidator).validate(request);
    verify(bankClient, never()).authorizePayment(any());
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void processPayment_whenBankCommunicationFails_shouldPropagateException() {
    PostPaymentRequest request = new PostPaymentRequest(
        "2222405343248880",
        4,
        2025,
        "GBP",
        100,
        "123"
    );

    when(paymentRequestValidator.validate(request)).thenReturn(Collections.emptyList());
    when(bankClient.authorizePayment(any(BankPaymentRequest.class)))
        .thenThrow(new BankCommunicationException("Bank service unavailable"));

    BankCommunicationException exception = assertThrows(
        BankCommunicationException.class,
        () -> paymentGatewayService.processPayment(request)
    );

    assertEquals("Bank service unavailable", exception.getMessage());

    verify(paymentRequestValidator).validate(request);
    verify(bankClient).authorizePayment(any(BankPaymentRequest.class));
    verify(paymentsRepository, never()).add(any(PaymentResponse.class));
  }
}