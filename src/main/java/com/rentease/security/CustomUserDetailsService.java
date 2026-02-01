package com.rentease.security;

import com.rentease.entity.User;
import com.rentease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        User user;

        try {
            // Try to parse as UUID first (used by JWT filter)
            UUID userId = UUID.fromString(userIdOrEmail);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userIdOrEmail));
        } catch (IllegalArgumentException e) {
            // Not a UUID, try as email
            user = userRepository.findByEmail(userIdOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userIdOrEmail));
        }

        return UserPrincipal.create(user);
    }
}
