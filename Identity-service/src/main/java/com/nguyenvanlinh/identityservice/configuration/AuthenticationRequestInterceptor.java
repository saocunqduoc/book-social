package com.nguyenvanlinh.identityservice.configuration;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

// modifer request before send to api
// need to get Header authorization and add to request before send
@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes != null
                ? servletRequestAttributes.getRequest().getHeader("Authorization")
                : null;

        log.info("Token {}", authHeader);
        if (StringUtils.hasText(authHeader)) requestTemplate.header("Authorization", authHeader);
    }
}
