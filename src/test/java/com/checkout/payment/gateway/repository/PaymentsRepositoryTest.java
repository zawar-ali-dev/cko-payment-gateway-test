package com.checkout.payment.gateway.repository;

import static org.assertj.core.api.Assertions.assertThat;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentsRepositoryTest {

  private PaymentsRepository repository;

  @BeforeEach
  void setUp() {
    repository = new PaymentsRepository();
  }

  @Test
  void whenPaymentIsAdded_thenItCanBeRetrievedById() {
    UUID id = UUID.randomUUID();
    PaymentResponse payment = new PaymentResponse(
        id,
        PaymentStatus.AUTHORIZED,
        "1234",
        12,
        2030,
        "USD",
        1000,
        null
    );

    repository.add(payment);

    Optional<PaymentResponse> result = repository.get(id);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(payment);
  }

  @Test
  void whenPaymentDoesNotExist_thenEmptyOptionalIsReturned() {
    Optional<PaymentResponse> result = repository.get(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  @Test
  void whenPaymentWithSameIdIsAdded_thenItOverwritesPreviousPayment() {
    UUID id = UUID.randomUUID();

    PaymentResponse first = new PaymentResponse(
        id,
        PaymentStatus.DECLINED,
        "1111",
        10,
        2025,
        "USD",
        500,
        null
    );

    PaymentResponse second = new PaymentResponse(
        id,
        PaymentStatus.AUTHORIZED,
        "2222",
        12,
        2030,
        "USD",
        1000,
        null
    );

    repository.add(first);
    repository.add(second);

    Optional<PaymentResponse> result = repository.get(id);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(second);
  }
}
