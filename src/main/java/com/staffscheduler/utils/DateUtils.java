package com.staffscheduler.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {
    public static boolean withinOneYear(Date startDate, Date endDate) {
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (startLocalDate.isAfter(endLocalDate))
            return false;
        Period period = Period.between(startLocalDate, endLocalDate);
        return period.getYears() == 0;
    }

    public static boolean filter(Date startDate, Date endDate, Date date) {
        if (!withinOneYear(startDate, endDate))
            return false;
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateLocal = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return dateLocal.compareTo(startLocalDate) >= 0 && dateLocal.compareTo(endLocalDate) <= 0;
    }
}
