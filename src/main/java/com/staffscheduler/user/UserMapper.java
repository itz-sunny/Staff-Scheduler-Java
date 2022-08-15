package com.staffscheduler.user;

import com.staffscheduler.user.model.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {
    @Override
    public User map(ResultSet rs, StatementContext ctx) throws SQLException {
        return User.builder()
                .userId(rs.getLong("userId"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .role(rs.getString("role"))
                .createdAt(rs.getDate("createdAt"))
                .updatedAt(rs.getDate("updatedAt"))
                .deleted(rs.getBoolean("deleted"))
                .build();
    }
}
