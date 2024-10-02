package com.nguyenvanlinh.identityservice.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.nguyenvanlinh.identityservice.constant.RolesConstant;
import com.nguyenvanlinh.identityservice.dto.request.*;
import com.nguyenvanlinh.identityservice.dto.response.AuthenticationResponse;
import com.nguyenvanlinh.identityservice.dto.response.IntrospectResponse;
import com.nguyenvanlinh.identityservice.entity.EmailVerifyToken;
import com.nguyenvanlinh.identityservice.entity.InvalidateToken;
import com.nguyenvanlinh.identityservice.entity.Role;
import com.nguyenvanlinh.identityservice.entity.User;
import com.nguyenvanlinh.identityservice.exception.AppException;
import com.nguyenvanlinh.identityservice.exception.ErrorCode;
import com.nguyenvanlinh.identityservice.repository.EmailVerifyTokenRepository;
import com.nguyenvanlinh.identityservice.repository.InvalidateTokenRepository;
import com.nguyenvanlinh.identityservice.repository.UserRepository;
import com.nguyenvanlinh.identityservice.repository.httpclient.OutBoundIdentityClient;
import com.nguyenvanlinh.identityservice.repository.httpclient.OutBoundUserClient;
import com.nguyenvanlinh.identityservice.repository.httpclient.ProfileClient;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidateTokenRepository invalidateTokenRepository;
    EmailVerifyTokenRepository emailVerifyTokenRepository;

    ProfileClient profileClient;
    OutBoundIdentityClient outBoundIdentityClient;
    OutBoundUserClient outBoundUserClient;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.expiration-duration}")
    protected Long EXPIRATION_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESHABLE_DURATION;

    @Value("${outbound.client-id}")
    @NonFinal
    String CLIENT_ID;

    @Value("${outbound.client-secret}")
    @NonFinal
    String CLIENT_SECRET;

    @Value("${outbound.redirect-uri}")
    @NonFinal
    String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";
    //
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        // get token từ request của authenticate
        var token = request.getToken();
        boolean isValid = true;
        try {
            // verify Jwt
            verifyToken(token, false);
        } catch (AppException e) {
            // nếu chưa verify/ expiration/ đã logout -> false
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    // Outbound _ Exchange token from google code
    public AuthenticationResponse outboundAuthentication(String code) {

        var response = outBoundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE: {}", response);
        // get Access Token
        var userInfo = outBoundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("USER INFO: {}", userInfo);

        // Onboard user when login with Google
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(RolesConstant.USER_ROLE).build());
        User user = new User();
        // nếu chưa có => tạo mới một user + profile với những gì google cung cấp từ oauth
        if (userRepository.findByUsername(userInfo.getEmail()).isEmpty()) {
            // onboard user
            user = userRepository.save(User.builder()
                    .username(userInfo.getEmail())
                    .email(userInfo.getEmail())
                    .roles(roles)
                    .emailVerified(true)
                    .build());
            // onboard profiles
            profileClient.createProfile(ProfileCreationRequest.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .userId(user.getId())
                    .firstName(userInfo.getGivenName())
                    .lastName(userInfo.getFamilyName())
                    .build());
        }
        var getToken = userRepository
                .findByUsername(userInfo.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("User RESPONSE: {}", getToken.getId() + getToken.getUsername());
        var token = generateToken(getToken);
        log.info("Token RESPONSE: {}", token);
        return AuthenticationResponse.builder().token(token).build();
    }
    // Xác thực email
    public IntrospectResponse verifyOtp(VerifiedEmailRequest request) {
        // Tìm mã OTP theo userId
        EmailVerifyToken emailVerifyToken = emailVerifyTokenRepository
                .findByUserIdAndToken(request.getUserId(), request.getOtp())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_USER_OR_OTP));

        // Cập nhật trạng thái xác thực của người dùng
        User user = userRepository
                .findById(emailVerifyToken.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setEmailVerified(true);
        userRepository.save(user);

        // Xóa mã OTP sau khi xác thực thành công
        emailVerifyTokenRepository.delete(emailVerifyToken);

        var isValid = user.isEmailVerified();
        return IntrospectResponse.builder().valid(isValid).build();
    }

    // Xác thực // POST : Token
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("{}", SIGNER_KEY);
        // get Username
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        // matches password
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.WRONG_USERNAME_PASSWORD);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    // logout
    public void logOut(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);
            String jti = signToken.getJWTClaimsSet().getJWTID();
            Date expirationTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .id(jti)
                    .expirationTime(expirationTime)
                    .build();

            invalidateTokenRepository.save(invalidateToken);
        } catch (AppException e) {
            log.info("Token already expired");
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefreshToken) throws JOSEException, ParseException {
        // Verify -> truyền vào signkey để mã hóa
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // parse token -> String và add exception
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Kiểm tra đã hết hạn chưa
        Date expiration = (isRefreshToken) // if refresh -> get plus time refresh else -> expirationTime của token
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // thực thi verify
        var verify = signedJWT.verify(verifier); // nếu đúng -> true else -> false
        // nếu chưa được verify và jwt đã hết hạn => unauthenticate
        if (!verify && !expiration.after(new Date())) { // check expiration có after now kh nếu đúng => true
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        // xác định JwtId nếu có -> JWT logout bị disable
        if (invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }

    // refresh token
    public AuthenticationResponse refreshRequest(RefreshTokenRequest request) throws ParseException, JOSEException {
        var signJWt = verifyToken(request.getToken(), true);

        var jti = signJWt.getJWTClaimsSet().getJWTID();
        Date expirationTime = signJWt.getJWTClaimsSet().getExpirationTime();

        InvalidateToken invalidateToken =
                InvalidateToken.builder().id(jti).expirationTime(expirationTime).build();

        invalidateTokenRepository.save(invalidateToken);

        var username = signJWt.getJWTClaimsSet().getSubject();
        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }
    // tạo token
    public String generateToken(User user) {
        // Header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512); // thuật toán HS512

        // Data trong body(nội dung gửi đi trong token) -> gọi là claim
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId()) // đại diện cho user đang nhập
                .issuer("nguyenvanlinh.com") // được issue từ ai? Thường là domain của Service
                .issueTime(new Date()) // thời gian tạo
                .expirationTime(new Date(Instant.now()
                        .plus(EXPIRATION_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())) // set time hết hạn vd 1 tiếng
                // tạo ID để thao tác như logout/ refresh
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user)) // scope take role of user
                .build();

        // Payload: nhận vào claimset
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        // cần Header và Payload
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        // Tiếp đến cần ký token : String metric dùng chung khóa ký và giải. Còn cách khác
        // lên https://generate-random.org/encryption-key-generator để key chuỗi 32 bytes 256 bit
        // save in yaml
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.info("Cannot create token!");
            throw new AppException(ErrorCode.CAN_NOT_CREATE_TOKEN);
        }
    }

    // build scope từ một user to get role -> claim ở payload
    // update: get user -> get roles bao gồm cả permissions của roles
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        // add roles
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                // add permissions
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }
}
