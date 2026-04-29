package com.checkout.payment.gateway.metric;

public enum GatewayMetric {

  PAYMENT_PROCESSED("payments.processed");

  private final String metricName;

  GatewayMetric(String metricName) {
    this.metricName = metricName;
  }

  public String getMetricName() {
    return metricName;
  }
}
