package com.track.moneta.backend.repositories;



import com.track.moneta.backend.models.Category;
import com.track.moneta.backend.models.Expense;
import com.track.moneta.backend.models.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import java.time.LocalDate;

public class ExpenseSpecification {

    public static Specification<Expense> hasUser(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<Expense> hasCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<Expense, Category> categoryJoin = root.join("category");
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<Expense> hasStartDate(LocalDate startDate) {
        if (startDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("expenseDate"), startDate);
    }

    public static Specification<Expense> hasEndDate(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("expenseDate"), endDate);
    }
}