package com.staffscheduler.user.repository;

import com.staffscheduler.authentication.model.Role;
import com.staffscheduler.authentication.service.BCryptPasswordEncoderService;
import com.staffscheduler.exception.DBOperationException;
import com.staffscheduler.exception.DuplicateUserException;
import com.staffscheduler.user.UserMapper;
import com.staffscheduler.user.model.*;
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

@Singleton
public class UserRepository {

    @Inject
    private DataSource dataSource;

    @Inject
    private BCryptPasswordEncoderService bCryptPasswordEncoderService;

    private final static Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    private final static String SQL_CREATE_USER = "INSERT INTO users (name, email, password, deleted) VALUES (?, ?, ?, ?)";
    private final static String SQL_CREATE_ROLE = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";

    private final static String SQL_UPDATE_USER = "UPDATE users SET name = COALESCE(? , name), email = COALESCE(?, email), password = COALESCE(?, password), updated_at = now() WHERE id = ? AND deleted IS NOT true";

    private final static String SQL_UPDATE_ROLE = "UPDATE user_roles SET role = ?, updated_at = now() WHERE user_id = ?";

    private final static String SQL_DELETE_USER = "UPDATE users SET deleted = true, updated_at = now() WHERE user_id = ?";
    private final static String SQL_EXISTS = "SELECT EXISTS (SELECT 1 FROM users where email = ? AND deleted IS NOT TRUE)";

    private final static String SQL_QUERY_USER = "SELECT u.id as userId, name, email, role, u.created_at as createdAt, u.updated_at as updatedAt, deleted from users u, user_roles r WHERE u.id = r.user_id AND u.id = ? AND u.deleted IS NOT true";

    private final static String SQL_QUERY_ALL_USER = "SELECT u.id as userId, name, email, role, u.created_at as createdAt, u.updated_at as updatedAt, deleted from users u, user_roles r WHERE u.id = r.user_id AND u.deleted IS NOT true";
    private final static String SQL_QUERY_ROLE_BY_USER_ID = "SELECT role from user_roles where user_id = ?";
    
    private final static String SQL_QUERY_USER_SORTED = "SELECT u.id as userId, u.name as name, u.email as email, u.created_at as createdAt, u.updated_at as updatedAt, r.role AS role, sum(shift_hours) AS totalShiftHours FROM users u INNER JOIN roles r ON u.id = r.user_id INNER JOIN schedules s ON u.id = s.user_id AND s.createdAt BETWEEN ? AND ? WHERE u.deleted = false GROUP BY u.id, r.role HAVING totalShiftHours IS NOT NULL ORDER BY totalShiftHours DESC";
    public Optional<User> createUser(RegisterDto registerDto) {

        Jdbi jdbi = Jdbi.create(dataSource);

        try (Handle handle = jdbi.open()) {
            Optional<Boolean> isDuplicateUser = userAlreadyExists(registerDto.getEmail());

            if (isDuplicateUser.isPresent() && isDuplicateUser.get()) {
                LOGGER.error("User with same email {} already exists", registerDto.getEmail());
                throw new DuplicateUserException(String.format("User with same email %s already exists", registerDto.getEmail()));
            }

            final Long[] userId = new Long[1];

            handle.useTransaction(handle1 -> {
                 ResultBearing resultBearing = handle1.createUpdate(SQL_CREATE_USER)
                        .bind(0, registerDto.getName())
                        .bind(1, registerDto.getEmail())
                        .bind(2, bCryptPasswordEncoderService.encode(registerDto.getPassword()))
                        .bind(3, false)
                        .executeAndReturnGeneratedKeys("id");

                if (Objects.isNull(resultBearing))
                    throw new DBOperationException("Failed to insert new user");

                 userId[0] = resultBearing.mapTo(Long.class).first();

                int inserts = handle1.createUpdate(SQL_CREATE_ROLE)
                        .bind(0, userId[0])
                        .bind(1, Role.STAFF.name())
                        .execute();

                if (inserts == 0)
                    throw new DBOperationException("Failed to insert new role");
            });
            return Optional.of(
                    User.builder()
                            .userId(userId[0])
                            .name(registerDto.getName())
                            .email(registerDto.getEmail())
                            .role(Role.STAFF.name())
                            .build()
            );
        } catch (JdbiException | DBOperationException | DuplicateUserException e) {
            LOGGER.error("Exception in creating new user for registerDto: {}", registerDto, e);
            throw e;
        }
    }

    public Optional<Boolean> userAlreadyExists(String email) {

        Jdbi jdbi = Jdbi.create(dataSource);
        Optional<Boolean> result = Optional.empty();

        try (Handle handle = jdbi.open()) {
            result = handle.createQuery(SQL_EXISTS)
                    .bind(0, email)
                    .mapTo(Boolean.class)
                    .findFirst();
        } catch (JdbiException e) {
            LOGGER.error("Exception in checking if user already exists for email: {}", email, e);
            throw e;
        }

        return result;
    }

    public boolean updateRoleByUserId(UpdateRoleRequest request) {

        Jdbi jdbi = Jdbi.create(dataSource);
        int updates = 0;

        try (Handle handle = jdbi.open()) {
            updates = handle.createUpdate(SQL_UPDATE_ROLE)
                    .bind(0, request.getRole().name())
                    .bind(1, request.getUserId())
                    .execute();

        } catch (JdbiException e) {
            LOGGER.error("Exception in updating role for request: {}", request, e);
        }

        return updates > 0;
    }

    public boolean updateUserById(UserUpdateRequest request) {

        Jdbi jdbi = Jdbi.create(dataSource);
        int updates = 0;

        try (Handle handle = jdbi.open()) {
            updates = handle.createUpdate(SQL_UPDATE_USER)
                    .bind(0, request.getName())
                    .bind(1, request.getEmail())
                    .bind(2, request.getPassword() != null && !request.getPassword().isEmpty() ? bCryptPasswordEncoderService.encode(request.getPassword()) : null)
                    .bind(3, request.getUserId())
                    .execute();

        } catch (JdbiException e) {
            LOGGER.error("Exception in updating user for userId: {}", request.getUserId(), e);
        }
        return updates > 0;
    }

    public boolean deleteUserById(long userId) {

        Jdbi jdbi = Jdbi.create(dataSource);

        try (Handle handle = jdbi.open()) {

            handle.useTransaction((handle1) -> {
                int updates = handle1.createUpdate(SQL_DELETE_USER)
                        .bind(0, userId)
                        .execute();
                if (updates == 0)
                    throw new DBOperationException("Failed to delete user for userId: " + userId);

                boolean deleted = handle1.createUpdate("UPDATE schedules SET deleted = true WHERE user_id = ?")
                        .bind(0, userId)
                        .execute() > 0;
                if (!deleted)
                    throw new DBOperationException("Failed to delete all schedules for userId: " + userId);
            });

            return true;

        } catch (JdbiException | DBOperationException e) {
            LOGGER.error("Exception in deleting user for userId: {}", userId, e);
            return false;
        }
    }

    public Optional<User> fetchUserById(long userId) {

        Jdbi jdbi = Jdbi.create(dataSource);

        Optional<User> result = Optional.empty();

        try (Handle handle = jdbi.open()) {
            result = handle.createQuery(SQL_QUERY_USER)
                    .bind(0, userId)
                    .map(new UserMapper())
                    .findFirst();

        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching user for userId: {}", userId, e);
        }

        return result;
    }

    public List<User> fetchAllUsers() {
        Jdbi jdbi = Jdbi.create(dataSource);
        List<User> result = new ArrayList<>();
        try (Handle handle = jdbi.open()) {
            result = handle.createQuery(SQL_QUERY_ALL_USER)
                    .map(new UserMapper())
                    .list();

        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching users", e);
        }

        return result;
    }

    public List<SortedUser> fetchAllUsersSorted(Date startDate, Date endDate) {
        Jdbi jdbi = Jdbi.create(dataSource);
        List<SortedUser> result = new ArrayList<>();
        try (Handle handle = jdbi.open()) {
            result = handle.createQuery(SQL_QUERY_USER_SORTED)
                    .map((rs, ctx) -> {
                        SortedUser user = new SortedUser();
                        user.setUserId(rs.getLong("userId"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));
                        user.setTotalShiftHours(rs.getInt("totalShiftHours"));
                        user.setCreatedAt(rs.getDate("createdAt"));
                        user.setUpdatedAt(rs.getDate("createdAt"));

                        return user;
                    }).list();

        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching sorted users", e);
        }

        return result;
    }

    public Optional<Role> fetchRoleByUserId(long userId) {
        Jdbi jdbi = Jdbi.create(dataSource);

        Optional<Role> result = Optional.empty();

        try (Handle handle = jdbi.open()) {
            result = handle.createQuery(SQL_QUERY_ROLE_BY_USER_ID)
                    .bind(0, userId)
                    .mapTo(Role.class)
                    .findFirst();

        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching user for userId: {}", userId, e);
        }

        return result;
    }
}
