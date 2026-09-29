# Changelog

Tous les changements notables sont documentés ici.

Format basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/).
Ce projet respecte le [Semantic Versioning](https://semver.org/lang/fr/).

---

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

---

## [1.1.0] - 2026-09-02

Mise à jour de parité avec le SDK JS (`sangho-sdk-js`).

### Added
- Nouvelles ressources : `account()` (`retrieve`), `addresses()` (CRUD complet),
  `terminal()` (`readers()`/`sessions()`/`offline()`), `sandbox()` (`reset`).
- `customers().listPaymentMethods(id)`.
- `checkoutSessions().expire(id)`.
- `invoices().getPdfUrl(id)`.
- `paymentLinks().archive(id)` / `paymentLinks().restore(id)`.
- `transactions().update(id, payload)` / `transactions().cancel(id)`.
- `apps().keys(id)`.
- `subscriptions().reactivate(id)` (distinct de `resume`, comme dans le SDK JS).
- `security().addAllowedIps(ips)` / `security().removeAllowedIps(ips)`.
- `webhooks().disable(id)` / `webhooks().enable(id)` / `webhooks().listDeliveries(id, params)` / `webhooks().retrieveDelivery(id, deliveryId)`.
- `HttpClient(..., maxRetries)` et `SanghoClient.Builder.maxRetries(int)` configurables.
- `SanghoClient.constructEvent(...)` exposé statiquement, en plus de `WebhooksResource.constructEvent(...)`.
- Exceptions réseau distinctes : `SanghoNetworkException`, `SanghoTimeoutException`
  (les `IOException` brutes ne remontent plus telles quelles).
- Chaque exception expose désormais un `getType()` (catégorie large), à l'image du SDK JS.
- Headers `X-Sangho-SDK` et `X-Sangho-Environment` sur chaque requête.
- Validation HTTPS du `baseUrl` (refuse l'envoi de la clé API en clair, sauf
  `localhost`/`127.0.0.1`) et validation stricte du format de clé API.
- `.github/workflows/ci.yml` : tests sur la matrice Java 21/25/26.
- `.github/workflows/release.yml` : automatisation de release (bump de
  version, tag git, publication Maven Central), adapté du workflow du SDK JS.
- Profile Maven `integration` (`mvn test -Pintegration`) pour isoler les
  tests d'intégration des tests unitaires en CI.

### Changed
- `baseUrl` par défaut : `https://api.sangho.ga/v1` (au lieu de `https://api.sangho.com/v1` — mauvais domaine).
- Préfixes de clé API valides : `pk_prod_` / `sk_prod_` / `pk_test_` / `sk_test_`
  (au lieu de `pk_live_` / `sk_live_` qui n'existent pas côté backend).
- **`SecurityResource` réécrite entièrement.** L'ancienne version exposait
  une forme CRUD générique (`list`/`create`/`update`/`delete`) qui ne
  correspond à aucune route réelle du backend — probablement générée à
  partir d'un template sans jamais être vérifiée contre le SDK JS. Remplacée
  par `retrieve()`/`update()`/`addAllowedIps()`/`removeAllowedIps()` sur les
  bonnes routes (`/security/me/`, `/security/update_me/`).
- Le retry sur 429 respecte désormais `retry_after` (délai serveur) en
  priorité sur le backoff exponentiel.
- **Boucle de retry corrigée** : après épuisement des tentatives, l'ancien
  code effectuait un appel HTTP supplémentaire (inutile) en dehors de la
  boucle pour obtenir le résultat final. La réponse déjà reçue est
  maintenant réutilisée directement.
- `pom.xml` : `java.version`/`maven.compiler.release` → **21** (LTS) au lieu
  de 25. La dernière version publiée (JDK 26, mars 2026) n'est pas visée en
  plancher de compatibilité : la plupart des systèmes de production Java
  tournent encore sur la LTS précédente, et une lib serveur doit viser la
  compatibilité la plus large réaliste plutôt que la version la plus
  récente. Testé en CI sur 21, 25 (LTS courante) et 26 (dernière publiée).
- `pom.xml` : auteur, `url`, `scm`, `issueManagement` alignés sur le
  `package.json` du SDK JS (`github.com/sangho-sdks/sangho.java`).
- README : quickstart entièrement réécrit — l'ancien importait
  `io.sangho.Sangho` (classe/package inexistants ; le vrai client est
  `com.sangho.SanghoClient`), appelait `.secretKey(...)` (le builder attend
  `.apiKey(...)`) et `.getId()` sur des `record` Java qui n'ont pas de
  getters (accesseur réel : `.id()`).
- Makefile : cible `publish` corrigée — `mvn deploy` seul n'atteint pas
  Maven Central sans le profile `release` (GPG + Sonatype) ; `test`
  n'excluait pas les tests d'intégration (`@Tag("integration")`) avant que
  la config Surefire ne soit ajoutée à `pom.xml`.

### Fixed
- **Bug bloquant, indépendant de la parité JS** : les classes `*CreateParams`/
  `*UpdateParams` (`CustomerCreateParams`, `PaymentIntentCreateParams`,
  `ProductCreateParams`, `InvoiceCreateParams`, `RefundCreateParams`,
  `SubscriptionCreateParams`, `WebhookCreateParams`, `CustomerUpdateParams`,
  `ProductUpdateParams`) n'avaient ni getters ni annotations Jackson —
  Jackson ne pouvait littéralement pas les sérialiser. **Tout appel à
  `create()`/`update()` avec ces objets levait une
  `SanghoException: No serializer found for class ...`** au lieu d'envoyer
  la requête. Découvert en testant réellement le SDK contre un serveur HTTP
  (le test `CustomersResourceTest.testValidationError` existant en était
  déjà affecté). Corrigé en ajoutant
  `@JsonAutoDetect(fieldVisibility = ANY)` sur ces 9 classes (+
  `@JsonProperty` sur les champs camelCase qui doivent être envoyés en
  snake_case : `paymentMethod`, `dueDate`), plutôt que de réutiliser
  `toMap()` (qui aurait transformé `amount` en chaîne de caractères dans le
  JSON au lieu d'un nombre).
- `SanghoRateLimitException` propage désormais `raw` (perdu silencieusement
  avant — `getRaw()` renvoyait toujours une map vide sur un 429).
- Comparaison de `code.equals("public_key_not_allowed")` désormais
  insensible à la casse (le backend renvoie parfois `PUBLIC_KEY_NOT_ALLOWED`).
- `checkoutSessions().retrieve()` n'exige plus de clé secrète — le backend
  autorise explicitement la clé publique sur cette route (page de
  confirmation côté navigateur).
- Suite de tests mise à jour en conséquence (domaine `.ga`, préfixes
  `_prod_`) ; nouveau `HttpClientTest.java` couvrant les fixes ci-dessus.

---

## [1.0.0] - 2026-04-01

### Added

- Version initiale du SDK
- Support de toutes les ressources : apps, customers, products, payment_intents,
  checkout_sessions, invoices, transactions, refunds, subscriptions,
  payment_methods, webhooks, payment_links, addresses, partners
- Gestion complète des erreurs (auth, validation, rate limit, réseau)
- Pagination via ListResponse
