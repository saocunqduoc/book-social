package com.nguyenvanlinh.identityservice.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nguyenvanlinh.identityservice.dto.request.UserCreationRequest;
import com.nguyenvanlinh.identityservice.dto.request.UserUpdateRequest;
import com.nguyenvanlinh.identityservice.dto.response.UserResponse;
import com.nguyenvanlinh.identityservice.entity.Role;
import com.nguyenvanlinh.identityservice.entity.User;
import com.nguyenvanlinh.identityservice.exception.AppException;
import com.nguyenvanlinh.identityservice.mapper.UserMapper;
import com.nguyenvanlinh.identityservice.repository.RoleRepository;
import com.nguyenvanlinh.identityservice.repository.UserRepository;
import com.nguyenvanlinh.identityservice.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @Container
    static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:latest");

    @DynamicPropertySource
    static void configureMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MY_SQL_CONTAINER::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    private UserCreationRequest request;
    private UserUpdateRequest userUpdateRequest;
    private UserMapper userMapper;
    private UserResponse userResponse;
    private User user;
    private LocalDate dob;
    private Role role;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void initData() {

        role = Role.builder().name("ROLE_USER").description("User default role").build();
        roleRepository.save(role);

        dob = LocalDate.of(1990, 1, 1);

        request = UserCreationRequest.builder()
                .username("saocunqduoc")
                .firstName("Linh")
                .lastName("Van Nguyen")
                .password("12345678910")
                .dob(dob)
                .build();
        userUpdateRequest = UserUpdateRequest.builder()
                .password("1@Linh2003")
                .lastName("Nguyen Van")
                .build();

        userResponse = UserResponse.builder()
                .id("cf0600f538b3")
                .username("saocunqduoc")
                .firstName("Linh")
                .lastName("Van Nguyen")
                .dob(dob)
                .build();

        user = User.builder()
                .id("cf0600f538b3")
                .username("saocunqduoc")
                .firstName("Linh")
                .lastName("Van Nguyen")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);
        when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(role));
        // WHEN
        var user = userService.createUser(request);

        // THEN
        Assertions.assertThat(user.getId()).isEqualTo("cf0600f538b3");
        Assertions.assertThat(user.getUsername()).isEqualTo("saocunqduoc");
    }

    @Test
    void createUser_userAlreadyExists_throwException() {
        // GIVEN
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        // WHEN / THEN
        AppException exception =
                Assertions.catchThrowableOfType(() -> userService.createUser(request), AppException.class);

        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002); // Mã lỗi USER_EXISTED
    }
    // get Info success
    @Test
    @WithMockUser(username = "saocunqduoc") // mock user => khong can authentication
    void getMyInfo_validRequest_success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        var resp = userService.getMyInfo();
        Assertions.assertThat(resp.getUsername()).isEqualTo("saocunqduoc");
    }
    // get Info fail
    @Test
    @WithMockUser(username = "saocunqduoc") // mock user => khong can authentication
    void getMyInfo_userNotFound_Error() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(null));

        var exception = assertThrows(AppException.class, () -> userService.getMyInfo());
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);
    }
    // delete user
    @Test
    @WithMockUser(
            username = "admin",
            roles = {"ADMIN"})
    void deleteUser_validRequest_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        userService.deleteUser(user.getId());
    }
    // get Users success
    @Test
    @WithMockUser(
            username = "admin",
            roles = {"ADMIN"})
    void getUsers_validRequest_success() {
        when(userRepository.findAll().stream().toList()).thenReturn(List.of(user));

        var resp = userService.getUsers();
        Assertions.assertThat(resp).isNotNull();
    }
    // get User by ID success
    @Test
    @WithMockUser(
            username = "saocunqduoc",
            roles = {"USER"})
    void getUserById_validRequest_success() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        var resp = userService.getUser("cf0600f538b3");
        Assertions.assertThat(resp.getId()).isEqualTo("cf0600f538b3");
    }
    // getUser by ID fail
    @Test
    @WithMockUser(
            username = "saocunqduoc",
            roles = {"USER"})
    void getUserById_userNotExist_fail() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        var exception = assertThrows(AppException.class, () -> userService.getUser("cf0600f538b3"));
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);
    }
    // updateUser
    @Test
    @WithMockUser(
            username = "saocunqduoc",
            roles = {"USER"})
    void updateUser_validRequest_success() {
        // GIVEN
        // đúng theo thứ tự trong update Service
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        userService.updateUser(user.getId(), userUpdateRequest);
        when(userRepository.save(any())).thenReturn(user);
        // WHERE
        // THEN
        Assertions.assertThat(user.getUsername()).isEqualTo("saocunqduoc");
        Assertions.assertThat(user.getLastName()).isEqualTo("Nguyen Van");
    }
    // updateUser fail
    @Test
    @WithMockUser(
            username = "saocunqduoc",
            roles = {"USER"})
    void updateUser_usernameNotExist_fail() {
        // GIVEN
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        // WHEN
        var mapper = assertThrows(AppException.class, () -> userService.updateUser(user.getId(), userUpdateRequest));
        // THEN
        Assertions.assertThat(mapper.getErrorCode().getCode()).isEqualTo(1005);
    }
}
