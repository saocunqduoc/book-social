package com.nguyenvanlinh.identityservice.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nguyenvanlinh.event.dto.NotificationEvent;
import com.nguyenvanlinh.identityservice.constant.RolesConstant;
import com.nguyenvanlinh.identityservice.dto.request.UserCreationRequest;
import com.nguyenvanlinh.identityservice.dto.request.UserUpdateRequest;
import com.nguyenvanlinh.identityservice.dto.response.UserResponse;
import com.nguyenvanlinh.identityservice.entity.EmailVerifyToken;
import com.nguyenvanlinh.identityservice.entity.Role;
import com.nguyenvanlinh.identityservice.entity.User;
import com.nguyenvanlinh.identityservice.exception.AppException;
import com.nguyenvanlinh.identityservice.exception.ErrorCode;
import com.nguyenvanlinh.identityservice.mapper.ProfileMapper;
import com.nguyenvanlinh.identityservice.mapper.UserMapper;
import com.nguyenvanlinh.identityservice.repository.EmailVerifyTokenRepository;
import com.nguyenvanlinh.identityservice.repository.RoleRepository;
import com.nguyenvanlinh.identityservice.repository.UserRepository;
import com.nguyenvanlinh.identityservice.repository.httpclient.ProfileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    EmailVerifyTokenRepository emailVerifyTokenRepository;

    UserMapper userMapper;

    PasswordEncoder passwordEncoder;

    // Profile service
    ProfileClient profileClient;
    ProfileMapper profileMapper;

    // Kafka
    KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(RolesConstant.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setEmailVerified(false);

        try {
            user = userRepository.save(user);

            // Tạo mã xác thực (token)
            // Tạo mã xác thực (OTP)
            String otp = generateOTP(); // Tạo mã xác thực 6 chữ số
            EmailVerifyToken emailVerifyToken =
                    EmailVerifyToken.builder().userId(user.getId()).token(otp).build(); // Lưu mã OTP
            emailVerifyTokenRepository.save(emailVerifyToken);
            log.info("Email verification token created : {}", otp);

            var profileRequest = profileMapper.toProfileCreationRequest(request);
            profileRequest.setUserId(user.getId());
            profileClient.createProfile(profileRequest);

            String emailBody = createEmailBody(user.getUsername(), user.getId(), otp);

            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(request.getEmail())
                    .subject("Welcome to Book Social")
                    .body(emailBody)
                    .build();

            // Publish message to kafka
            kafkaTemplate.send("notification-delivery", notificationEvent);

        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public Void sendEmailToVerification() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository
                .findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String otp = generateOTP(); // Tạo mã xác thực 6 chữ số
        EmailVerifyToken emailVerifyToken =
                EmailVerifyToken.builder().userId(user.getId()).token(otp).build(); // Lưu mã OTP
        emailVerifyTokenRepository.save(emailVerifyToken);

        String emailBody = createEmailBody(user.getUsername(), user.getId(), otp);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .subject("The latest step to onboard your account!")
                .body(emailBody)
                .build();

        // Publish message to kafka
        kafkaTemplate.send("notification-delivery", notificationEvent);
        return null;
    }

    // getInfo bằng token authenticate hiện tại -> Không cần endpoint là id user
    public UserResponse getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository
                .findById(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("In GET myInfo method : {}", authentication.getName());
        return userMapper.toUserResponse(user);
    }

    // thực hiện việc xác thực trước khi truy cập vào hàm
    @PreAuthorize("hasRole('ADMIN')")
    // không phù hợp vì khi gọi role sẽ ưu tiên tìm role thay vì quyền
    // => nên gọi hasAuthority thay vì hasRole vì 1 người có thể có nhiều Role
    //    @PreAuthorize("hasAuthority('APPROVE_POST')")
    public List<UserResponse> getUsers() {
        log.info("In GET Users method"); // Nếu không có ROLE ADMIN sẽ không thể truy cập Method -> không hiện log
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.id == authentication.name")
    public UserResponse getUser(String id) {
        log.info(
                "In getUser method"); // Sẽ truy cập Method và hiện log bất kể có ROLE hay không. Có -> return. Không ->
        // Acces denied
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String idUser, UserUpdateRequest request) {
        User user = userRepository.findById(idUser).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // kiểm tra xem tài khoản muốn update có giống với thông tin xác thực không
        // mapper update
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // convert convert để get role hiện tại của user
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String idUser) {
        userRepository.deleteById(idUser);
    }

    private String generateOTP() {
        SecureRandom secureRandom = new SecureRandom();
        int otp = 100000 + secureRandom.nextInt(900000); // Tạo số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(otp);
    }
    // Tạo template cho email body
    private String createEmailBody(String username, String userId, String otp) {
        return "<h1>Welcome to Book Social, " + username + "!</h1>"
                + "<p>Thank you for creating an account with us. Please verify your email address using the OTP below:</p>"
                + "<h2>Your OTP: "
                + otp + "</h2>" + "<p>Or click the button below to verify your email:</p>"
                + "<a href='http://localhost:8668/api/v1/identity/auth/verify?userId="
                + userId + "&otp=" + otp + "'"
                + "style='display:inline-block; padding:10px 20px; background-color:#4CAF50; color:white; text-decoration:none; border-radius:5px;'>Verify Email</a>"
                + "<p>If you did not create an account, please ignore this email.</p>";
    }
}
