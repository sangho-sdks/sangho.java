# Sangho Java SDK

SDK officiel Java pour l'API [Sangho](https://sangho.ga) — paiements XAF pour l'Afrique.

[![Maven Central](https://img.shields.io/maven-central/v/com.sangho/sangho-java.svg)](https://central.sonatype.com/artifact/com.sangho/sangho-java)
[![CI](https://github.com/sangho-sdks/sangho.java/actions/workflows/ci.yml/badge.svg)](https://github.com/sangho-sdks/sangho.java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Installation

**Maven :**

```xml
<dependency>
  <groupId>com.sangho</groupId>
  <artifactId>sangho-java</artifactId>
  <version>1.1.0</version>
</dependency>
```

**Gradle :**

```groovy
implementation 'com.sangho:sangho-java:1.1.0'
```

Compatible Java 21 et supérieur (testé en CI sur 21, 25 et 26).

## Quickstart

```java
import com.sangho.SanghoClient;
import com.sangho.model.PaymentIntent;
import com.sangho.param.PaymentIntentCreateParams;

SanghoClient client = SanghoClient.builder()
    .apiKey("sk_prod_...")
    .build();

// Créer un payment intent
PaymentIntent intent = client.paymentIntents().create(
    PaymentIntentCreateParams.builder()
        .amount(5000)
        .customer("cust_xxx")
        .build()
);

System.out.println(intent.id());
```

## Gestion des erreurs

```java
import com.sangho.exception.*;

try {
    var intent = client.paymentIntents().create(
        PaymentIntentCreateParams.builder().amount(5000).customer("cust_xxx").build());
} catch (SanghoAuthException e) {
    System.out.println("Clé API invalide");
} catch (SanghoRateLimitException e) {
    System.out.println("Trop de requêtes, retenter après " + e.getRetryAfter() + "s");
} catch (SanghoValidationException e) {
    System.out.println(e.getParam() + ": " + e.getMessage());
} catch (SanghoException e) {
    System.out.println(e.getType() + " — " + e.getMessage() + " (" + e.getStatusCode() + ")");
}
```

## Documentation

La documentation complète est disponible sur [docs.sangho.ga/api/sdks/java](https://docs.sangho.ga/api/sdks/java/).

## Ressources disponibles

`account` · `addresses` · `apps` · `customers` · `products` · `paymentIntents` ·
`checkoutSessions` · `invoices` · `transactions` · `refunds` · `subscriptions` ·
`paymentMethods` · `receipts` · `webhooks` · `paymentLinks` · `security` ·
`partners` · `terminal` · `sandbox`

## Contribuer

Voir [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

Voir [CHANGELOG.md](CHANGELOG.md).

## Licence

MIT
