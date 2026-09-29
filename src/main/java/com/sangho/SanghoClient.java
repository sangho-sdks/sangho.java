package com.sangho;

import com.sangho.http.HttpClient;
import com.sangho.resource.*;

import java.time.Duration;

/**
 * Point d'entrée du SDK Java de Sangho.
 *
 * <pre>{@code
 * SanghoClient sangho = SanghoClient.builder().apiKey("sk_test_xxx").build();
 * Customer c = sangho.customers().retrieve("cust_xxx");
 * }</pre>
 */
public class SanghoClient {

    public static final String DEFAULT_BASE_URL = "https://api.sangho.ga/v1";

    private final AccountResource account;
    private final AddressesResource addresses;
    private final AppsResource apps;
    private final CheckoutSessionsResource checkoutSessions;
    private final CustomersResource customers;
    private final InvoicesResource invoices;
    private final PartnersResource partners;
    private final PaymentIntentsResource paymentIntents;
    private final PaymentLinksResource paymentLinks;
    private final PaymentMethodsResource paymentMethods;
    private final ProductsResource products;
    private final ReceiptsResource receipts;
    private final RefundsResource refunds;
    private final SandboxResource sandbox;
    private final SecurityResource security;
    private final SubscriptionsResource subscriptions;
    private final TerminalResource terminal;
    private final TransactionsResource transactions;
    private final WebhooksResource webhooks;

    private SanghoClient(Builder b) {
        HttpClient http = new HttpClient(b.apiKey, b.baseUrl, b.timeout, b.maxRetries, Thread::sleep);
        this.account = new AccountResource(http);
        this.addresses = new AddressesResource(http);
        this.apps = new AppsResource(http);
        this.checkoutSessions = new CheckoutSessionsResource(http);
        this.customers = new CustomersResource(http);
        this.invoices = new InvoicesResource(http);
        this.partners = new PartnersResource(http);
        this.paymentIntents = new PaymentIntentsResource(http);
        this.paymentLinks = new PaymentLinksResource(http);
        this.paymentMethods = new PaymentMethodsResource(http);
        this.products = new ProductsResource(http);
        this.receipts = new ReceiptsResource(http);
        this.refunds = new RefundsResource(http);
        this.sandbox = new SandboxResource(http);
        this.security = new SecurityResource(http);
        this.subscriptions = new SubscriptionsResource(http);
        this.terminal = new TerminalResource(http);
        this.transactions = new TransactionsResource(http);
        this.webhooks = new WebhooksResource(http);
    }

    public AccountResource account()                   { return account; }
    public AddressesResource addresses()               { return addresses; }
    public AppsResource apps()                         { return apps; }
    public CheckoutSessionsResource checkoutSessions() { return checkoutSessions; }
    public CustomersResource customers()               { return customers; }
    public InvoicesResource invoices()                 { return invoices; }
    public PartnersResource partners()                 { return partners; }
    public PaymentIntentsResource paymentIntents()     { return paymentIntents; }
    public PaymentLinksResource paymentLinks()         { return paymentLinks; }
    public PaymentMethodsResource paymentMethods()     { return paymentMethods; }
    public ProductsResource products()                 { return products; }
    public ReceiptsResource receipts()                 { return receipts; }
    public RefundsResource refunds()                   { return refunds; }
    public SandboxResource sandbox()                   { return sandbox; }
    public SecurityResource security()                 { return security; }
    public SubscriptionsResource subscriptions()       { return subscriptions; }
    public TerminalResource terminal()                 { return terminal; }
    public TransactionsResource transactions()         { return transactions; }
    public WebhooksResource webhooks()                 { return webhooks; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = HttpClient.DEFAULT_MAX_RETRIES;

        public Builder apiKey(String v) {
            this.apiKey = v;
            return this;
        }

        /** Hôte de l'API (HTTPS obligatoire hors localhost). Défaut : {@value SanghoClient#DEFAULT_BASE_URL}. */
        public Builder baseUrl(String v) {
            this.baseUrl = v;
            return this;
        }

        public Builder timeout(Duration v) {
            this.timeout = v;
            return this;
        }

        /** Nouvelles tentatives sur 429, 5xx et erreurs réseau (backoff exponentiel). Défaut : 3. */
        public Builder maxRetries(int v) {
            this.maxRetries = v;
            return this;
        }

        public SanghoClient build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("apiKey is required.");
            }
            return new SanghoClient(this);
        }
    }
}
