package com.bacos.mokengeli.biloko.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletRequest;


@Configuration
public class FeignCookieConfig {

    @Autowired
    private HttpServletRequest request;

    @Bean
    public RequestInterceptor cookieInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                if (request != null && request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("accessToken".equals(cookie.getName())) {
                            String accessToken = cookie.getValue();
                            requestTemplate.header("Cookie", "accessToken=" + accessToken);
                        }
                    }
                }
            }
        };
    }
}
