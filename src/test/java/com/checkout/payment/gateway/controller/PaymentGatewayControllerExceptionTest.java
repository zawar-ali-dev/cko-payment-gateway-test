package com.checkout.payment.gateway.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.exception.BankCommunicationException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerExceptionTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @Test
  void whenBankCommunicationExceptionOccurs_then503IsReturned() throws Exception {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new BankCommunicationException("Bank down"));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.message")
            .value("Unable to communicate with acquiring bank. Please try again later."));
  }

  @Test
  void whenUnexpectedExceptionOccurs_then500IsReturned() throws Exception {
    PostPaymentRequest request = new PostPaymentRequest(
        "4111111111111111",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new RuntimeException("Something went very wrong"));

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message")
            .value("An unexpected error occurred"));
  }
}
