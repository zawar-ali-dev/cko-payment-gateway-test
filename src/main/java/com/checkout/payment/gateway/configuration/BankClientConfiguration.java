package com.checkout.payment.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BankClientConfiguration {

  @Value("${bank.sim.url}")
  private String bankApiUrl;

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder.baseUrl(bankApiUrl).build();
  }
}
