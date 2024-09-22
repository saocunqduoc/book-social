package com.nguyenvanlinh.identityservice.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
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

            String emailBody = "<!DOCTYPE html>" + "<html lang=\"vi\">"
                    + "<head>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;}"
                    + ".container { background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); max-width: 600px; margin: auto; padding: 20px;}"
                    + "h1 { color: #333; text-align: center; }"
                    + "p {color: #555; line-height: 1.5;}"
                    + ".otp {font-size: 24px;font-weight: bold;color: #000000;text-align: center;margin: 20px 0; }"
                    + ".button { display: block; width: 100%; background-color: #007bff; color: #ffffff; /* Màu chữ */ padding: 10px; text-align: center; text-decoration: none;border-radius: 5px;font-weight: bold; margin-top: 20px; }\n"
                    + "a { color: #007bff; text-decoration: none; /* Bỏ gạch chân */ }"
                    + ".footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px;}"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class=\"container\">"
                    + "<h1>Chào mừng bạn đến với Book Social, "
                    + request.getUsername() + "!</h1>" + "<p>Cảm ơn bạn đã đăng ký tài khoản</p>"
                    + "<p>Để xác thực tài khoản của bạn, vui lòng sử dụng mã OTP dưới đây:</p>"
                    + "<div class=\"otp\">"
                    + otp + "</div>" + // Thay đổi mã OTP ở đây
                    "<p>Hoặc nhấp vào nút bên dưới để xác thực tài khoản của bạn:</p>"
                    + "<a href='http://localhost:8080/verify-email?otp="
                    + otp + "' class=\"button\">Xác thực tài khoản</a>" + // Thay đổi đường dẫn ở đây
                    "<div class=\"footer\">"
                    + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                    + "<p>Chúc bạn một ngày tốt lành!</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
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
    // getInfo bằng token authenticate hiện tại -> Không cần endpoint là id user
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        context.getAuthentication();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("In GET myInfo method");
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

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(String id) {
        log.info(
                "In getUser method"); // Sẽ truy cập Method và hiện log bất kể có ROLE hay không. Có -> return. Không ->
        // Acces denied
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

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
}
