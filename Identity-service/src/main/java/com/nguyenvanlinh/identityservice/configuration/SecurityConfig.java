package com.nguyenvanlinh.identityservice.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // config public endpoint
    protected static final String[] PUBLIC_ENDPOINTS = {
        "/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refreshToken"
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // xác thực và cấp quyền cho người xem có quyền truy cập vào endpoint đó không
        httpSecurity.authorizeHttpRequests(request -> request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                .permitAll()
                //                        .requestMatchers(HttpMethod.GET,"/users").hasAuthority("ROLE_ADMIN") // cũ :
                // SCOPE_ADMIN
                //                        .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name()) // sử
                // dụng hasRole thay cho hasAuthority
                .anyRequest()
                .authenticated());
        // Xác thực người dùng bằng decoder JWT khi login
        httpSecurity.oauth2ResourceServer(oauth2 ->
                // decode để JWT để xác thực
                oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(customJwtDecoder)
                                // convert SCOPE -> Role
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        // authentication
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        // vô hiệu hóa CSRF ( Cross-Site Request Forgery.) trong lúc build dự án để có thể POST, PUT, DELETE
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }
    // config cors de co the run api on website
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // allow to access from what web
        //        corsConfiguration.addAllowedOrigin("http://localhost:3000");
        corsConfiguration.addAllowedOrigin("*");
        // allow method can call in origin
        corsConfiguration.addAllowedMethod("*");
        // allow header
        corsConfiguration.addAllowedHeader("*");

        // khai bao cors theo tung endpoint
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // customer SCOPE_ -> ROLE_

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }
    // decode JWT
    //    @Bean
    //    JwtDecoder jwtDecoder() { // verify token
    //        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
    //        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
    //                .macAlgorithm(MacAlgorithm.HS512)
    //                .build();
    //    }
    // decoder for any
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
