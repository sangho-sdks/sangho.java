package com.sangho;

import com.sangho.http.HttpClient;
import com.sangho.resource.*;
import java.time.Duration;

/**
 * Main entry point for the Sangho Java SDK.
 *
 * <pre>{@code
 * SanghoClient sangho = SanghoClient.builder().apiKey("sk_test_xxx").build();
 * Customer c = sangho.customers().retrieve("cust_xxx");
 * }</pre>
 */
public class SanghoClient {

    private final AppsResource apps;
    private final CustomersResource customers;
    private final ProductsResource products;
    private final PaymentIntentsResource paymentIntents;
    private final PaymentLinksResource paymentLinks;
    private final CheckoutSessionsResource checkoutSessions;
    private final InvoicesResource invoices;
    private final TransactionsResource transactions;
    private final RefundsResource refunds;
    private final SubscriptionsResource subscriptions;
    private final PaymentMethodsResource paymentMethods;
    private final ReceiptsResource receipts;
    private final WebhooksResource webhooks;
    private final SecurityResource security;
    private final PartnersResource partners;

    private SanghoClient(Builder b) {
        HttpClient http = new HttpClient(b.apiKey, b.baseUrl, b.timeout);
        this.apps = new AppsResource(http);
        this.customers = new CustomersResource(http);
        this.products = new ProductsResource(http);
        this.paymentIntents = new PaymentIntentsResource(http);
        this.paymentLinks = new PaymentLinksResource(http);
        this.checkoutSessions = new CheckoutSessionsResource(http);
        this.invoices = new InvoicesResource(http);
        this.transactions = new TransactionsResource(http);
        this.refunds = new RefundsResource(http);
        this.subscriptions = new SubscriptionsResource(http);
        this.paymentMethods = new PaymentMethodsResource(http);
        this.receipts = new ReceiptsResource(http);
        this.webhooks = new WebhooksResource(http);
        this.security = new SecurityResource(http);
        this.partners = new PartnersResource(http);
    }

    public AppsResource apps() {
        return apps;
    }

    public CustomersResource customers() {
        return customers;
    }

    public ProductsResource products() {
        return products;
    }

    public PaymentIntentsResource paymentIntents() {
        return paymentIntents;
    }

    public PaymentLinksResource paymentLinks() {
        return paymentLinks;
    }

    public CheckoutSessionsResource checkoutSessions() {
        return checkoutSessions;
    }

    public InvoicesResource invoices() {
        return invoices;
    }

    public TransactionsResource transactions() {
        return transactions;
    }

    public RefundsResource refunds() {
        return refunds;
    }

    public SubscriptionsResource subscriptions() {
        return subscriptions;
    }

    public PaymentMethodsResource paymentMethods() {
        return paymentMethods;
    }

    public ReceiptsResource receipts() {
        return receipts;
    }

    public WebhooksResource webhooks() {
        return webhooks;
    }

    public SecurityResource security() {
        return security;
    }

    public PartnersResource partners() {
        return partners;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String apiKey;
        private String baseUrl = "https://api.sangho.com/v1";
        private Duration timeout = Duration.ofSeconds(30);

        public Builder apiKey(String v) {
            this.apiKey = v;
            return this;
        }

        public Builder baseUrl(String v) {
            this.baseUrl = v;
            return this;
        }

        public Builder timeout(Duration v) {
            this.timeout = v;
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
