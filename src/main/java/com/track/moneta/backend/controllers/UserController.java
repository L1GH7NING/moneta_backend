package com.track.moneta.backend.controllers;

import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.dto.UserUpdateDTO;
import com.track.moneta.backend.repositories.BudgetRepository;
import com.track.moneta.backend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final BudgetRepository budgetRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUserProfile(
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        // Fetch fresh user data from database instead of returning cached principal
        UserDTO freshUser = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(freshUser);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserDTO> updateUserDetails(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateDTO updateDTO,
            @AuthenticationPrincipal UserDTO currentUser
    ) {
        Long authenticatedUserId = currentUser.getId();

        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }
        UserDTO updatedUser = userService.updateUser(authenticatedUserId, updateDTO);

        return ResponseEntity.ok(updatedUser);
    }
}