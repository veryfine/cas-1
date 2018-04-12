package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;

import static org.junit.Assert.*;

/**
 * Tests for the {@code FileTrustStoreSslSocketFactory} class, checking for self-signed
 * and missing certificates via a local truststore.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
public class FileTrustStoreSslSocketFactoryTests {

    private static final ClassPathResource RESOURCE = new ClassPathResource("truststore.jks");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable() {
        final var clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://self-signed.badssl.com"));
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWithCertAvailable2() {
        final var clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://untrusted-root.badssl.com"));
    }

    @Test
    public void verifyTrustStoreNotFound() {
        this.thrown.expect(FileNotFoundException.class);
        sslFactory(new FileSystemResource("test.jks"), "changeit");
    }

    @Test
    public void verifyTrustStoreBadPassword() {
        this.thrown.expect(IOException.class);
        sslFactory(RESOURCE, "invalid");
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyForValidEndpointWithNoCert() {
        final var clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("https://www.google.com"));
    }

    @Test
    public void verifyTrustStoreLoadingSuccessfullyWihInsecureEndpoint() {
        final var clientFactory = new SimpleHttpClientFactoryBean();
        clientFactory.setSslSocketFactory(sslFactory());
        final HttpClient client = clientFactory.getObject();
        assertTrue(client.isValidEndPoint("http://wikipedia.org"));
    }

    private static SSLConnectionSocketFactory sslFactory(final Resource resource, final String password) {
        return new SSLConnectionSocketFactory(new DefaultCasSslContext(resource,
                password,
                KeyStore.getDefaultType()).getSslContext());
    }

    private static SSLConnectionSocketFactory sslFactory() {
        return sslFactory(RESOURCE, "changeit");
    }
}
