package com.staffscheduler.schedule.repository;

import com.staffscheduler.exception.DBOperationException;
import com.staffscheduler.schedule.ScheduleBaseMapper;
import com.staffscheduler.schedule.model.Schedule;
import com.staffscheduler.schedule.model.ScheduleBase;
import com.staffscheduler.user.model.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.result.ResultBearing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class ScheduleRepository {

    @Inject
    private DataSource dataSource;

    private final static Logger LOGGER = LoggerFactory.getLogger(ScheduleRepository.class);

    private final static String SQL_CREATE = "INSERT INTO schedules (user_id, work_date, shift_hours) VALUES (?, ?, ?)";

    private final static String SQL_QUERY_BY_USER_ID = "SELECT id, work_date, shift_hours, FROM schedules WHERE user_id = ? AND work_date BETWEEN ? AND ? AND deleted IS NOT true";

    private final static String SQL_UPDATE = "UPDATE schedules SET work_date = COALESCE(?, work_date), shift_hours = COALESCE(?, shift_hours), updated_at = now() WHERE id = ?";

    private final static String SQL_DELETE_BY_ID = "UPDATE schedules SET deleted = true, updated_at = now() WHERE id = ?";

    private final static String SQL_DELETE_ALL_BY_USER_ID = "UPDATE schedules SET deleted = true, updated_at = now() WHERE user_id = ?";

    public Optional<Schedule> createSchedule(User user, ScheduleBase scheduleBase) {

        Jdbi jdbi = Jdbi.create(dataSource);

        try (Handle handle = jdbi.open()) {
            ResultBearing resultBearing = handle.createUpdate(SQL_CREATE)
                    .bind(0, user.getUserId())
                    .bind(1, scheduleBase.getWorkDate())
                    .bind(2, scheduleBase.getShiftHours())
                    .executeAndReturnGeneratedKeys("id");

            if (Objects.isNull(resultBearing))
                throw new DBOperationException("Failed to insert schedule");

            long scheduleId = resultBearing.mapTo(Long.class).first();
            scheduleBase.setScheduleId(scheduleId);

            return Optional.of(mapToSchedule(scheduleBase, user));
        } catch (JdbiException | DBOperationException e) {
            LOGGER.error("Exception in creating new schedule: {} for user: {}", scheduleBase, user, e);
            return Optional.empty();
        }
    }

    public List<Schedule> fetchScheduleByUser(User user, Date start, Date end) {
        Jdbi jdbi = Jdbi.create(dataSource);

        try (Handle handle = jdbi.open()) {

            List<ScheduleBase> list = handle.createQuery(SQL_QUERY_BY_USER_ID)
                    .bind(0, user.getUserId())
                    .bind(1, start)
                    .bind(2, end)
                    .map(new ScheduleBaseMapper())
                    .list();

            return list.stream()
                    .map(s -> mapToSchedule(s, user))
                    .collect(Collectors.toList());

        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching schedules for userId: {}, start date: {}, end date: {}", user.getUserId(), start, end, e);
            return Collections.emptyList();
        }
    }

    public boolean updateScheduleById(ScheduleBase scheduleBase) {
        Jdbi jdbi = Jdbi.create(dataSource);
        int updates = 0;

        try (Handle handle = jdbi.open()) {
             updates = handle.createUpdate(SQL_UPDATE)
                    .bind(0, scheduleBase.getWorkDate())
                    .bind(1, scheduleBase.getShiftHours())
                    .bind(2, scheduleBase.getScheduleId())
                    .execute();

        } catch (JdbiException e) {
            LOGGER.error("Exception in updating schedule: {}", scheduleBase, e);
        }

        return updates > 0;
    }

    public boolean deleteAllSchedulesByUserId(long userId) {
        Jdbi jdbi = Jdbi.create(dataSource);
        int updates = 0;

        try (Handle handle = jdbi.open()) {
            updates = handle.createUpdate(SQL_DELETE_ALL_BY_USER_ID)
                    .bind(0, userId)
                    .execute();

        } catch (JdbiException e) {
            LOGGER.error("Exception in deleting schedules for userId: {}", userId, e);
        }

        return updates > 0;
    }

    public boolean deleteScheduleById(long scheduleId) {
        Jdbi jdbi = Jdbi.create(dataSource);
        int updates = 0;

        try (Handle handle = jdbi.open()) {
            updates = handle.createUpdate(SQL_DELETE_BY_ID)
                    .bind(0, scheduleId)
                    .execute();

        } catch (JdbiException e) {
            LOGGER.error("Exception in deleting schedule with id: {}", scheduleId, e);
        }

        return updates > 0;
    }

    private Schedule mapToSchedule(ScheduleBase scheduleBase, User user) {
        Schedule schedule = new Schedule();
        schedule.setUser(user);
        schedule.setShiftHours(scheduleBase.getShiftHours());
        schedule.setWorkDate(scheduleBase.getWorkDate());

        return schedule;
    }

}
