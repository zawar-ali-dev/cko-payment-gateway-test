package com.checkout.payment.gateway;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * This integration test assumes that the bank simulator is running on localhost:8080 as a prereq
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentGatewayIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  private PostPaymentRequest validPaymentRequest;

  @BeforeEach
  void setUp() {
    validPaymentRequest = new PostPaymentRequest(
        "4111111111111111",
        12,
        2030,
        "USD",
        1000,
        "123"
    );
  }

  @Test
  void whenValidPayment_thenReturnsAuthorized() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validPaymentRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value("1111"))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(1000));
  }

  @Test
  void whenBankDeclinesPayment_thenReturnsDeclined() throws Exception {
    // Card ending with even number triggers decline in simulator
    PostPaymentRequest declinedRequest = new PostPaymentRequest(
        "4111111111111112",
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(declinedRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.DECLINED.getName()));
  }

  @Test
  void whenPaymentInvalid_thenReturnsRejected() throws Exception {
    PostPaymentRequest invalidRequest = new PostPaymentRequest(
        "",  // Missing card number
        12,
        2030,
        "USD",
        1000,
        "123"
    );

    mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(PaymentStatus.REJECTED.getName()))
        .andExpect(jsonPath("$.errors").isNotEmpty());
  }

  @Test
  void whenPaymentWithIdExists_thenReturnsPayment() throws Exception {
    // post a valid payment
    String responseBody = mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validPaymentRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Extract the payment ID
    UUID paymentId = objectMapper.readTree(responseBody).get("id").asText() == null
        ? UUID.randomUUID() : UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + paymentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(PaymentStatus.AUTHORIZED.getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value("1111"))
        .andExpect(jsonPath("$.amount").value(1000))
        .andExpect(jsonPath("$.currency").value("USD"));
  }

  @Test
  void whenPaymentWithIdNotExists_thenReturns404() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }
}
