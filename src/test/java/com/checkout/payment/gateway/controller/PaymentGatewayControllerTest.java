package com.checkout.payment.gateway.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PaymentsRepository paymentsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void whenValidPaymentIsProcessed_thenReturnsAuthorized() throws Exception {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value("1111"))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(1000));
  }

  @Test
  void whenBankDeclinesPayment_thenReturnsDeclined() throws Exception {
    // Card ending with even number triggers decline in bank simulator
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111112",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.DECLINED.getName()));
  }

  @Test
  void whenPaymentHasInvalidData_thenReturnsRejected() throws Exception {
    // Invalid payload (short card number)
    PostPaymentRequest request = new PostPaymentRequest(
        "12",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()));
  }

  @Test
  void whenPaymentWithIdExists_thenCorrectPaymentIsReturned() throws Exception {
    PaymentResponse payment = new PaymentResponse(
        UUID.randomUUID(),
        PaymentStatus.AUTHORIZED,
        "1234",
        12,
        2025,
        "USD",
        1000,
        null
    );
    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + payment.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.status().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.cardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.expiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.expiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.currency()))
        .andExpect(jsonPath("$.amount").value(payment.amount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExist_then404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }
}
