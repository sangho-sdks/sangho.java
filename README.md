# Sangho Java SDK

SDK officiel Java pour l'API [Sangho](https://sangho.ga) — paiements XAF pour l'Afrique.

[![Docs](https://img.shields.io/badge/docs-docs.sangho.ga-navy)](https://docs.sangho.ga/api/sdks/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Installation

Java ≥ 17.

**Maven :**

```xml
<dependency>
  <groupId>com.sangho</groupId>
  <artifactId>sangho-java</artifactId>
  <version>0.1.4</version>
</dependency>
```

**Gradle :**

```groovy
implementation 'com.sangho:sangho-java:0.1.4'
```

## Démarrage

```java
import com.sangho.SanghoClient;
import com.sangho.model.Customer;
import com.sangho.model.PaymentIntent;
import com.sangho.param.CustomerCreateParams;
import com.sangho.param.PaymentIntentCreateParams;

SanghoClient sangho = SanghoClient.builder()
    .apiKey("sk_test_...")            // clé de test ; en production : sk_prod_...
    .build();

Customer customer = sangho.customers().create(
    CustomerCreateParams.builder().email("jean@example.com").name("Jean Ondo").build());

PaymentIntent intent = sangho.paymentIntents().create(
    PaymentIntentCreateParams.builder().amount(5000).customer(customer.id()).build());

System.out.println(intent.status());
```

Options du builder : `baseUrl(...)` (défaut `https://api.sangho.ga/v1`, HTTPS obligatoire hors `localhost`),
`timeout(Duration)` (défaut 30 s), `maxRetries(int)` (défaut 3).

Les clés commencent par `pk_test_`, `sk_test_` (bac à sable) ou `pk_prod_`, `sk_prod_` (production). Une **clé publique**
(`pk_…`) ne peut appeler que `checkoutSessions().retrieve(...)` ; tout le reste exige une clé secrète.

## Listes paginées

```java
ListResponse<Customer> page = sangho.customers().list(null);
page.count();   // total
page.data();    // éléments de la page (et non « results »)
page.next();    // URL de la page suivante ou null
```

## Gestion des erreurs

Toutes les erreurs héritent de `SanghoException` (non vérifiée) et exposent `getType()` (catégorie large),
`getCode()` (code métier précis du backend), `getStatusCode()`, `getParam()`, `getRequestId()` et `getRaw()` :

```java
try {
    sangho.paymentIntents().create(...);
} catch (SanghoValidationException e) {
    e.getType();         // "VALIDATION_ERROR"
    e.getCode();         // "AMOUNT_TOO_SMALL"
    e.getFieldErrors();  // {amount=[...]}
    e.getRequestId();    // à communiquer au support
} catch (SanghoRateLimitException e) {
    Thread.sleep(e.getRetryAfter() * 1000L);
} catch (SanghoNetworkException | SanghoTimeoutException e) {
    // la requête n'a pas abouti
}
```

| Classe                        | Statut | `getType()`            |
| ----------------------------- | ------ | ---------------------- |
| `SanghoAuthException`         | 401    | `AUTHENTICATION_ERROR` |
| `SanghoPublicKeyException`    | 403    | `PERMISSION_ERROR` (code `PUBLIC_KEY_NOT_ALLOWED`) |
| `SanghoPermissionException`   | 403    | `PERMISSION_ERROR`     |
| `SanghoNotFoundException`     | 404    | `NOT_FOUND_ERROR`      |
| `SanghoIdempotencyException`  | 409    | `CONFLICT_ERROR`       |
| `SanghoValidationException`   | 422    | `VALIDATION_ERROR`     |
| `SanghoRateLimitException`    | 429    | `RATE_LIMIT_ERROR`     |
| `SanghoNetworkException`      | —      | `NETWORK_ERROR`        |
| `SanghoTimeoutException`      | —      | `TIMEOUT_ERROR`        |

Les erreurs `429` (en respectant `retry_after`), `5xx` et réseau sont réessayées avec un backoff exponentiel
(`maxRetries`). Les autres `4xx` ne le sont jamais. Chaque `POST` envoie une `Idempotency-Key` (UUID).

## Webhooks

```java
Map<String, Object> event = WebhooksResource.constructEvent(rawBody, signatureHeader, secret, 300);
```

Lève `SanghoException` si la signature est invalide ou l'événement périmé.

## Ressources

`account` · `addresses` · `apps` · `checkoutSessions` · `customers` · `invoices` · `partners` (lecture seule) ·
`paymentIntents` · `paymentLinks` · `paymentMethods` · `products` · `receipts` · `refunds` · `sandbox` · `security` ·
`subscriptions` · `terminal` (`readers()`, `sessions()`, `offline()`) · `transactions` · `webhooks`.

Documentation complète : [docs.sangho.ga](https://docs.sangho.ga/api/sdks/java/).

## Développement

```bash
mvn test        # tests unitaires ; les tests d'intégration visent l'API réelle (SANGHO_TEST_SECRET_KEY)
mvn package     # construit le jar
```

Voir [CONTRIBUTING.md](CONTRIBUTING.md) et [CHANGELOG.md](CHANGELOG.md).

## Licence

MIT
