package com.track.moneta.backend.services.impl;

import com.track.moneta.backend.dto.UserDTO;
import com.track.moneta.backend.dto.UserUpdateDTO;
import com.track.moneta.backend.exceptions.APIException;
import com.track.moneta.backend.models.Budget;
import com.track.moneta.backend.models.User;
import com.track.moneta.backend.repositories.BudgetRepository;
import com.track.moneta.backend.repositories.UserRepository;
import com.track.moneta.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.track.moneta.backend.utility.CommonUtils.calculateBudgetPeriod;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private final BudgetRepository budgetRepository;

    private void updateActiveBudgetsForNewCycle(Long userId, User user) {

        LocalDate today = LocalDate.now();
        LocalDate[] newPeriod = calculateBudgetPeriod(user, today);
        LocalDate newStartDate = newPeriod[0];
        LocalDate newEndDate = newPeriod[1];
        LocalDateTime now = LocalDateTime.now();

        List<Budget> activeBudgets = budgetRepository.findActiveBudgetsByUserId(userId, newStartDate);

        List<Budget> budgetsToUpdate = budgetRepository.findByUserIdAndDateRange(userId, newStartDate, newEndDate);

        for (Budget budget : budgetsToUpdate) {
            // Check if the dates ACTUALLY need an update
            if (!budget.getStartDate().equals(newStartDate) || !budget.getEndDate().equals(newEndDate)) {
                budget.setStartDate(newStartDate);
                budget.setEndDate(newEndDate);
                budget.setUpdatedAt(now);
            }
        }

        // Batch save the updated entities
        budgetRepository.saveAll(budgetsToUpdate);
    }

    @Override
    public UserDTO updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));

        Integer oldBudgetStartDate = user.getBudgetStartDate();

        if (updateDTO.getName() != null && !updateDTO.getName().isBlank()) {
            user.setName(updateDTO.getName());
        }

        if (updateDTO.getEmail() != null && !updateDTO.getEmail().isBlank()) {
            Optional<User> existingUser = userRepository.findByEmail(updateDTO.getEmail());
            if(existingUser.isPresent()) {
                throw new APIException("Email is already in use");
            }
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getBudgetStartDate() != null) {
            user.setBudgetStartDate(updateDTO.getBudgetStartDate());
        }

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isBlank()) {
            String hashedPassword = passwordEncoder.encode(updateDTO.getPassword());
            user.setPassword(hashedPassword);
        }


        user.setUpdatedAt(LocalDateTime.now());


        User updatedUser = userRepository.save(user);

        if (updateDTO.getBudgetStartDate() != null && !oldBudgetStartDate.equals(updateDTO.getBudgetStartDate())) {
            updateActiveBudgetsForNewCycle(userId, updatedUser);
        }

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new APIException("User not found with id: " + userId));
        return modelMapper.map(user, UserDTO.class);
    }
}