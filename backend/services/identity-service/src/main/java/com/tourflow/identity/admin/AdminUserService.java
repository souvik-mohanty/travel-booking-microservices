package com.tourflow.identity.admin;

import com.tourflow.identity.admin.dto.AdminUserResponse;
import com.tourflow.identity.user.User;
import com.tourflow.identity.user.UserNotFoundException;
import com.tourflow.identity.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Admin-only account management: list/inspect every user, and enable/disable
// a specific account (User.enabled already gates login -- see AuthService --
// this is just the first API surface that lets anyone actually toggle it).
@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getUsers(String role) {
        List<User> users = role == null || role.isBlank()
                ? userRepository.findAll()
                : userRepository.findByRole(role.toUpperCase());

        return users.stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID id) {
        return AdminUserResponse.from(findOrThrow(id));
    }

    // Idempotent: disabling an already-disabled account is a no-op that
    // returns 200 unchanged, matching the status-transition convention in
    // docs/api/API-STANDARDS.md. A disabled account's existing access token
    // stays valid until it expires (JWTs are stateless) -- disabling only
    // blocks future logins and refreshes, same as AccountDisabledException's
    // existing behavior in AuthService.
    @Transactional
    public AdminUserResponse disableUser(UUID id) {
        User user = findOrThrow(id);

        if (!user.isEnabled()) {
            return AdminUserResponse.from(user);
        }

        user.setEnabled(false);

        return AdminUserResponse.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse enableUser(UUID id) {
        User user = findOrThrow(id);

        if (user.isEnabled()) {
            return AdminUserResponse.from(user);
        }

        user.setEnabled(true);

        return AdminUserResponse.from(userRepository.save(user));
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}
