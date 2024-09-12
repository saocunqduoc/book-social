package com.nguyenvanlinh.identityservice.configuration;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nguyenvanlinh.identityservice.constant.RolesConstant;
import com.nguyenvanlinh.identityservice.entity.Permission;
import com.nguyenvanlinh.identityservice.entity.Role;
import com.nguyenvanlinh.identityservice.entity.User;
import com.nguyenvanlinh.identityservice.repository.PermissionRepository;
import com.nguyenvanlinh.identityservice.repository.RoleRepository;
import com.nguyenvanlinh.identityservice.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository) {
        return args -> {
            log.info("Initializing application.....");
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(RolesConstant.USER_ROLE)
                        .description("User role")
                        .build());
                Permission createPost = new Permission("CREATE_POST", "Create Post");
                Permission updatePost = new Permission("UPDATE_POST", "Update Post");
                Permission approvePost = new Permission("APPROVE_POST", "Approve Post");
                Permission rejectPost = new Permission("REJECT_POST", "Reject Post");
                Permission deletePost = new Permission("DELETE_POST", "Delete Post");
                permissionRepository.saveAll(Set.of(createPost, updatePost, approvePost, rejectPost, deletePost));
                // add Role & Permission of user
                Role userRole = Role.builder()
                        .name(RolesConstant.USER_ROLE)
                        .description("User role")
                        .permissions(Set.of(createPost))
                        .build();
                roleRepository.save(userRole);
                // add Role & Permission of admin
                Role adminRole = roleRepository.save(Role.builder()
                        .name(RolesConstant.ADMIN_ROLE)
                        .description("Admin role")
                        .permissions(Set.of(createPost, updatePost, approvePost, rejectPost, deletePost))
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);
                // create Admin with first time run application
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
                log.info("Application initialization completed .....");
            }
        };
    }
}
