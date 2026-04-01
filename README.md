# Sangho Java SDK

SDK officiel Java pour l'API [Sangho](https://sangho.africa) — paiements XAF pour l'Afrique.

[![Maven Central](https://img.shields.io/maven-central/v/io.sangho/sangho-java.svg)](https://central.sonatype.com/artifact/io.sangho/sangho-java)
[![CI](https://github.com/sangho-sdks/sangho-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sangho-sdks/sangho-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Installation

**Maven :**

```xml
<dependency>
  <groupId>com.sangho</groupId>
  <artifactId>sangho-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Gradle :**

```groovy
implementation 'io.sangho:sangho-java:1.0.0'
```

## Quickstart

```java
import io.sangho.Sangho;
import io.sangho.model.PaymentIntent;

Sangho client = Sangho.builder()
    .secretKey("sk_live_...")
    .build();

// Créer un payment intent
PaymentIntent intent = client.paymentIntents().create(
    CreatePayloads.builder()
        .amount(5000)
        .currency("XAF")
        .customer("cust_xxx")
        .build()
);

System.out.println(intent.getId());
```

## Documentation

La documentation complète est disponible sur [docs.sangho.africa](https://docs.sangho.africa).

## Ressources disponibles

`apps` · `customers` · `products` · `paymentIntents` · `checkoutSessions` ·
`invoices` · `transactions` · `refunds` · `subscriptions` · `paymentMethods` ·
`webhooks` · `paymentLinks` · `addresses` · `partners`

## Contribuer

Voir [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

Voir [CHANGELOG.md](CHANGELOG.md).

## Licence

MIT
