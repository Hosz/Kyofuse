package com.hokyozu.kyofuse.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private ClientIpResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ClientIpResolver();
    }

    @Test
    void resolveReturnsRemoteAddrWhenNoHeadersPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("198.51.100.1");
    }

    @Test
    void resolveExtractsXClientIpFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-IP", "177.18.29.40");
        request.addHeader("CF-Connecting-IP", "203.0.113.50");
        request.addHeader("X-Forwarded-For", "198.51.100.20");
        request.setRemoteAddr("10.0.0.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("177.18.29.40");
    }

    @Test
    void resolveExtractsCfConnectingIpFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "203.0.113.50");
        request.addHeader("X-Forwarded-For", "198.51.100.20");
        request.setRemoteAddr("10.0.0.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("203.0.113.50");
    }

    @Test
    void resolveExtractsFirstIpFromXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("10.0.0.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("203.0.113.195");
    }

    @Test
    void resolveHandlesUnknownInXForwardedForGracefully() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown, 203.0.113.195");
        request.setRemoteAddr("10.0.0.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("203.0.113.195");
    }

    @Test
    void resolveStripsPortFromIpV4() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.10:8080");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("203.0.113.10");
    }

    @Test
    void resolveStripsPortFromBracketedIpV6() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "[2001:db8::1]:443");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("2001:db8::1");
    }

    @Test
    void resolveReturnsFallbackWhenRequestIsNull() {
        String ip = resolver.resolve(null);

        assertThat(ip).isEqualTo("127.0.0.1");
    }

    @Test
    void isLocalOrLoopbackIdentifiesPrivateAndLoopbackIps() {
        assertThat(resolver.isLocalOrLoopback("127.0.0.1")).isTrue();
        assertThat(resolver.isLocalOrLoopback("::1")).isTrue();
        assertThat(resolver.isLocalOrLoopback("10.0.0.1")).isTrue();
        assertThat(resolver.isLocalOrLoopback("192.168.1.100")).isTrue();
        assertThat(resolver.isLocalOrLoopback("172.16.0.5")).isTrue();
        assertThat(resolver.isLocalOrLoopback("localhost")).isTrue();
        assertThat(resolver.isLocalOrLoopback(null)).isTrue();
        assertThat(resolver.isLocalOrLoopback("unknown")).isTrue();

        assertThat(resolver.isLocalOrLoopback("8.8.8.8")).isFalse();
        assertThat(resolver.isLocalOrLoopback("200.189.1.5")).isFalse();
    }
}
