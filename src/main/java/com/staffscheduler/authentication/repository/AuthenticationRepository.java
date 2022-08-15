package com.staffscheduler.authentication.repository;

import com.staffscheduler.authentication.model.RegisteredUserDetails;
import com.staffscheduler.authentication.RegisteredUserDetailsMapper;
import com.staffscheduler.authentication.model.Role;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Optional;

import static com.staffscheduler.authentication.model.Role.ADMIN;
import static com.staffscheduler.authentication.model.Role.STAFF;

@Singleton
public class AuthenticationRepository {

    @Inject
    private DataSource dataSource;

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationRepository.class);

    private final static String SQL_USER = "SELECT id, name, email, password FROM users WHERE email = ? AND deleted IS NOT TRUE";
    private final static String SQL_ROLE = "SELECT role FROM user_roles WHERE user_id = ?";
    public Optional<RegisteredUserDetails> fetchRegisteredUserDetails(String email) {

        Jdbi jdbi = Jdbi.create(dataSource);

        try (Handle handle = jdbi.open()) {
            return handle.createQuery(SQL_USER)
                    .bind(0, email)
                    .map(new RegisteredUserDetailsMapper())
                    .findFirst();
        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching registered user details for email: {}", email, e);
            return Optional.empty();
        }
    }

    public Optional<Role> fetchRole(String userId) {

        Jdbi jdbi = Jdbi.create(dataSource);
        Optional<Role> role = Optional.empty();
        try (Handle handle = jdbi.open()) {
             Optional<String> result = handle.createQuery(SQL_ROLE)
                    .bind(0, Long.parseLong(userId))
                    .mapTo(String.class)
                    .findFirst();

             if (result.isPresent()) {
                 return Optional.of(ADMIN.name().equals(result.get()) ? ADMIN : STAFF);
             }
        } catch (JdbiException e) {
            LOGGER.error("Exception in fetching role for userId: {}", userId, e);
        }
        return role;
    }
}
