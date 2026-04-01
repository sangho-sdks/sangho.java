# Sangho Java SDK

Official Java SDK for the [Sangho](https://sangho.com) payment platform.

## Requirements

- Java 17+
- Maven 3.9+ or Gradle 8+

## Installation

### Maven

```xml
<dependency>
    <groupId>com.sangho</groupId>
    <artifactId>sangho-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.sangho:sangho-sdk:1.0.0'
```

## Quick Start

```java
SanghoClient sangho = SanghoClient.builder()
    .apiKey("sk_test_xxx")
    .build();

// List customers
ListResponse<Customer> list = sangho.customers().list(
    CustomerListParams.builder().status("active").pageSize(20).build()
);

// Create a payment intent
PaymentIntent intent = sangho.paymentIntents().create(
    PaymentIntentCreateParams.builder()
        .amount(25000)
        .customer("cust_xxx")
        .build()
);

// Confirm
PaymentIntent confirmed = sangho.paymentIntents().confirm(intent.id(), null);
```

## Error Handling

```java
try {
    sangho.invoices().pay("inv_xxx", null);
} catch (SanghoValidationException e) {
    e.getFieldErrors().forEach((field, errors) ->
        System.out.println(field + ": " + errors));
} catch (SanghoNotFoundException e) {
    System.out.println("Invoice not found");
} catch (SanghoRateLimitException e) {
    System.out.println("Retry after: " + e.getRetryAfter() + "s");
}
```

## Webhook Verification

```java
Map<String, Object> event = WebhooksResource.constructEvent(
    requestBodyBytes,
    request.getHeader("Sangho-Signature"),
    "whsec_xxx",
    300
);
```
