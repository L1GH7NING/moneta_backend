package com.track.moneta.backend.utility;

import com.track.moneta.backend.models.User;

import java.time.LocalDate;

public final class CommonUtils {

    private CommonUtils(){
        // private constructor to prevent instantiation
    }

    public static LocalDate[] calculateBudgetPeriod(User user, LocalDate today) {
        int budgetStartDay = user.getBudgetStartDate();

        LocalDate periodStart;
        if (today.getDayOfMonth() >= budgetStartDay) {
            periodStart = today.withDayOfMonth(budgetStartDay);
        } else {
            periodStart = today.minusMonths(1).withDayOfMonth(budgetStartDay);
        }

        LocalDate nextPeriodStart = periodStart.plusMonths(1);
        LocalDate periodEnd = nextPeriodStart.minusDays(1);

        return new LocalDate[]{periodStart, periodEnd};
    }

}
