package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.BankPaymentRequest;
import com.checkout.payment.gateway.model.BankPaymentResponse;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service responsible for orchestrating payment processing.
 * <p>
 * Handles validation, communicates with the bank, stores payments, and generates
 * {@link PaymentResponse} objects.
 */
@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);
  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;
  private final PaymentRequestValidator paymentRequestValidator;

  public PaymentGatewayService(
      PaymentsRepository paymentsRepository,
      BankClient bankClient,
      PaymentRequestValidator paymentRequestValidator
  ) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
    this.paymentRequestValidator = paymentRequestValidator;
  }

  public Optional<PaymentResponse> getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id);
  }

  public PaymentResponse processPayment(PostPaymentRequest paymentRequest) {
    List<String> errors = paymentRequestValidator.validate(paymentRequest);

    if (!errors.isEmpty()) {
      LOG.warn("Payment request rejected due to validation errors: {}", errors);

      return new PaymentResponse(
          null,
          PaymentStatus.REJECTED,
          null,
          null,
          null,
          null,
          null,
          errors
      );
    }

    UUID paymentId = UUID.randomUUID();
    PaymentStatus status;
    String lastFourDigits = paymentRequest.cardNumberLastFour();

    LOG.debug("Processing payment request for card ending in {}", lastFourDigits);

      BankPaymentRequest bankRequest = new BankPaymentRequest(
          paymentRequest.cardNumber(),
          paymentRequest.expiryDate(),
          paymentRequest.currency(),
          paymentRequest.amount(),
          paymentRequest.cvv()
      );

      BankPaymentResponse bankResponse = bankClient.authorizePayment(bankRequest);

      if (bankResponse.authorized()) {
        status = PaymentStatus.AUTHORIZED;
        LOG.info("Payment {} authorized by bank", paymentId);
      } else {
        status = PaymentStatus.DECLINED;
        LOG.info("Payment {} declined by bank", paymentId);
      }

    PaymentResponse payment = new PaymentResponse(
        paymentId,
        status,
        lastFourDigits,
        paymentRequest.expiryMonth(),
        paymentRequest.expiryYear(),
        paymentRequest.currency(),
        paymentRequest.amount(),
        null
    );

    paymentsRepository.add(payment);

    return payment;
  }
}
