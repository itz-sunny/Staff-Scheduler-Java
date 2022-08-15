package com.staffscheduler.user.service;

import com.staffscheduler.authentication.model.Role;
import com.staffscheduler.exception.DBOperationException;
import com.staffscheduler.exception.DuplicateUserException;
import com.staffscheduler.user.model.*;
import com.staffscheduler.user.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Singleton
public class UserService {

    @Inject
    private UserRepository userRepository;

    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public Optional<User> register(RegisterDto registerDto) throws DBOperationException, DuplicateUserException, JdbiException {
        return userRepository.createUser(registerDto);
    }

    public boolean updateUserById(UserUpdateRequest request) {
        return userRepository.updateUserById(request);
    }

    public boolean updateRoleByUserId(UpdateRoleRequest request) {
        return userRepository.updateRoleByUserId(request);
    }

    public boolean deleteUserById(long userId) {
        return userRepository.deleteUserById(userId);
    }

    public Optional<User> fetchUserById(long userId) {
        return userRepository.fetchUserById(userId);
    }

    public List<User> fetchAllUsers() { return userRepository.fetchAllUsers(); }

    public List<SortedUser> fetchAllUsersSorted(Date startDate, Date endDate) {

        return userRepository.fetchAllUsersSorted(startDate, endDate);
    }
    public Role fetchRoleByUserId(long userId) {
        Optional<Role> role = userRepository.fetchRoleByUserId(userId);
        return role.orElse(Role.STAFF);
    }
}
