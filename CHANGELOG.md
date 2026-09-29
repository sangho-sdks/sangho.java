# Changelog

Tous les changements notables sont documentés ici.

Format basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/).
Ce projet respecte le [Semantic Versioning](https://semver.org/lang/fr/).

---

## [Unreleased]

> **Versionnement.** La version 1.0.0 ci-dessous était interne : ce SDK n'a jamais été publié (Maven Central). La
> numérotation est réalignée sur celle du SDK JS (`@sanghosdk/js` 0.1.4, seul SDK publié), comme le demande
> `CONTRIBUTING.md` (« tous les SDKs sont versionnés de façon synchronisée »). Version courante : **0.1.4**.

### Fixed
- **Bloquant** : `customers().create(...)`, `products().create(...)` et toutes les méthodes prenant un objet de
  paramètres (`*Params.builder()`) échouaient (« No serializer found ») : le client HTTP sérialisait l'objet lui-même,
  qui n'a pas de getters. Les paramètres implémentent maintenant `RequestParams` et s'envoient via `toMap()`.
- **Bloquant** : le `pom.xml` exigeait le JDK 25 ; le SDK cible maintenant Java 17.
- Les clés de production `pk_prod_` / `sk_prod_` étaient refusées (le SDK n'acceptait que `pk_live_` / `sk_live_`,
  préfixe qui n'existe pas côté API).
- URL par défaut : `https://api.sangho.ga/v1` (et non `.com`).
- `SecurityResource` appelait `/security/{id}/` ; les routes sont `/security/me/` et `/security/update_me/`.
- `SanghoRateLimitException` lisait `retry_later` (clé inexistante) : lit maintenant `retry_after`, puis l'en-tête
  `Retry-After`.
- Le retry ne s'appliquait qu'à une partie des méthodes ; il couvre maintenant toutes les requêtes. Une réponse d'erreur
  non JSON (ex. 502 HTML) ne provoque plus d'exception de parsing.
- La signature des webhooks est comparée en temps constant.
- `checkoutSessions().retrieve(...)` accepte une clé publique (page de confirmation côté navigateur), comme les autres SDK.
- Le dossier `target/` (102 fichiers de build) était versionné malgré le `.gitignore`.

### Added
- Ressources `account`, `addresses`, `sandbox` (`reset`) et `terminal` (`readers`, `sessions`, `offline`).
- `apps().keys`, `checkoutSessions().expire`, `invoices().getPdfUrl`, `receipts().getPdfUrl`,
  `paymentIntents().update/delete`, `paymentLinks().archive/restore`, `paymentMethods().attach/detach/setDefault`,
  `transactions().update/cancel`, `subscriptions().reactivate`, `webhooks().enable/disable/listDeliveries/retrieveDelivery`.
- Erreurs typées comme le SDK JS : `getType()`, `getCode()` (code métier), `getParam()`, `getRequestId()`,
  `getDocUrl()` ; `SanghoNetworkException` et `SanghoTimeoutException`.
- Retry avec backoff exponentiel sur `429` (en respectant `retry_after`), tout `5xx` et les erreurs réseau
  (`maxRetries(int)`) ; en-têtes `X-Sangho-SDK` et `X-Sangho-Environment` ; validation de la clé et de l'URL de base.
- `LICENSE` (MIT), tests unitaires (client HTTP, alignement sur l'API).

### Changed
- **Breaking** : `ListResponse` expose `data()` (et non `results()`), conformément à la pagination réelle de l'API.
- **Breaking** : constructeurs des exceptions (`raw` en dernier argument, sans code par défaut).
- `customers().listPaymentMethods(id, params)` filtre `GET /payment-methods/?customer=<id>`.

### Removed
Méthodes qui appelaient des routes **inexistantes** côté API (elles répondaient 404/405) :
`customers().listTransactions` (l'API n'a pas de filtre `customer` sur les transactions), `invoices().finalize`,
`products().archive/restore`, `partners().create/update/delete` (ressource en lecture seule),
`paymentMethods().create/update/delete`, `receipts().create/update/delete`, `checkoutSessions().update`,
`security().create/delete`.

---

## [1.0.0] - 2026-04-01

### Added

- Version initiale du SDK
- Support de toutes les ressources : apps, customers, products, payment_intents,
  checkout_sessions, invoices, transactions, refunds, subscriptions,
  payment_methods, webhooks, payment_links, addresses, partners
- Gestion complète des erreurs (auth, validation, rate limit, réseau)
- Pagination via ListResponse
