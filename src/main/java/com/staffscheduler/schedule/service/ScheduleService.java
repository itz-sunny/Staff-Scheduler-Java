package com.staffscheduler.schedule.service;

import com.staffscheduler.exception.BadRequestException;
import com.staffscheduler.exception.InvalidUserException;
import com.staffscheduler.schedule.model.Schedule;
import com.staffscheduler.schedule.model.ScheduleBase;
import com.staffscheduler.schedule.model.ScheduleRequest;
import com.staffscheduler.schedule.repository.ScheduleRepository;
import com.staffscheduler.user.model.User;
import com.staffscheduler.user.service.UserService;
import com.staffscheduler.utils.DateUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class ScheduleService {

    @Inject
    private ScheduleRepository scheduleRepository;

    @Inject
    private UserService userService;

    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleService.class);

    public Optional<Schedule> createSchedule(ScheduleRequest scheduleRequest) {
        try {
            if (Objects.isNull(scheduleRequest.getShiftHours()) || scheduleRequest.getShiftHours() <= 0 || Objects.isNull(scheduleRequest.getWorkDate()))
                throw new BadRequestException("400", "Invalid Request");

            Optional<User> userOptional = userService.fetchUserById(scheduleRequest.getUserId());

            if (!userOptional.isPresent())
                throw new InvalidUserException("User not found");

            return scheduleRepository.createSchedule(userOptional.get(), scheduleRequest);
        } catch (Exception e) {
            LOGGER.error("Exception in creating schedule for request: {}", scheduleRequest, e);
        }

        return Optional.empty();
    }

    public List<Schedule> fetchScheduleByUserId(long userId, Date start, Date end) {
        try {
            boolean isWithinOneYear = DateUtils.withinOneYear(start, end);

            if (!isWithinOneYear)
                throw new BadRequestException("400", "difference between start and end date must be less than 1 year");

            Optional<User> userOptional = userService.fetchUserById(userId);

            if (!userOptional.isPresent())
                throw new InvalidUserException("User not found");
            return scheduleRepository.fetchScheduleByUser(userOptional.get(), start, end);
        } catch (Exception e) {
            LOGGER.error("Exception in fetching schedules for userId: {}", userId, e);
        }
        return Collections.emptyList();
    }

    public List<Schedule> fetchAllSchedules(Date start, Date end) {

        List<Schedule> result = new ArrayList<>();
        try {
            boolean isWithinOneYear = DateUtils.withinOneYear(start, end);
            if (!isWithinOneYear)
                throw new BadRequestException("400", "difference between start and end date must be less than 1 year");

            List<User> users = userService.fetchAllUsers();

            for (User user : users) {
                try {
                    List<Schedule> schedules = scheduleRepository.fetchScheduleByUser(user, start, end);
                    result.addAll(schedules);
                } catch (Exception e) {
                    LOGGER.error("Exception in fetching schedules for userId: {}", user.getUserId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in fetching schedules", e);
        }
        return result;
    }

    public boolean deleteScheduleById(long scheduleId) {
        return scheduleRepository.deleteScheduleById(scheduleId);
    }

    public boolean updateScheduleById(ScheduleBase request) {
        return scheduleRepository.updateScheduleById(request);
    }

    public boolean deleteAllSchedulesByUserId(long userId) {
        return scheduleRepository.deleteAllSchedulesByUserId(userId);
    }
}
