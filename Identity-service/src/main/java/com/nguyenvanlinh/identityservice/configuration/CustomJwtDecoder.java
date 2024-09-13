package com.nguyenvanlinh.identityservice.configuration;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import com.nguyenvanlinh.identityservice.dto.request.IntrospectRequest;
import com.nguyenvanlinh.identityservice.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.signerKey}")
    private String signerKey;

//    @Autowired
//    private AuthenticationService authenticationService;
//
//    private NimbusJwtDecoder nimbusJwtDecoder = null;

    // Khi đã authentication ở gateway -> không cần xử lý ở tầng service để không bị duplicate
    @Override
    public Jwt decode(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return new Jwt(token,
                    signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                    signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                    signedJWT.getHeader().toJSONObject(),
                    signedJWT.getJWTClaimsSet().getClaims());
        } catch (ParseException e) {
            throw new JwtException("Invalid Token");
        }
    }
//    @Override
//    public Jwt decode(String token) throws JwtException {
//        try {
//            var response = authenticationService.introspect(new IntrospectRequest(token));
//            if (!response.isValid()) {
//                throw new BadJwtException("Invalid token");
//            }
//        } catch (ParseException | JOSEException e) {
//            throw new BadJwtException(e.getMessage());
//        }
//        if (Objects.isNull(nimbusJwtDecoder)) {
//            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
//            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
//                    .macAlgorithm(MacAlgorithm.HS512)
//                    .build();
//        }
//        return nimbusJwtDecoder.decode(token);
//    }
}
