package org.upc.desktopbffservice.shared.infrastructure.security;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class FeignAuthorizationForwardingConfigurationTests {

    private final FeignAuthorizationForwardingConfiguration configuration = new FeignAuthorizationForwardingConfiguration();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsBearerAuthorizationHeaderToFeignRequests() {
        var request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var requestTemplate = new RequestTemplate();

        configuration.bearerTokenForwardingRequestInterceptor().apply(requestTemplate);

        assertThat(requestTemplate.headers())
                .containsEntry(HttpHeaders.AUTHORIZATION, java.util.List.of("Bearer access-token"));
    }

    @Test
    void doesNotForwardMissingAuthorizationHeader() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        var requestTemplate = new RequestTemplate();

        configuration.bearerTokenForwardingRequestInterceptor().apply(requestTemplate);

        assertThat(requestTemplate.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }
}
