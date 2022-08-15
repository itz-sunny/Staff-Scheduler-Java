package com.staffscheduler.authentication;

import com.staffscheduler.authentication.model.RegisteredUserDetails;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisteredUserDetailsMapper implements RowMapper<RegisteredUserDetails> {

    @Override
    public RegisteredUserDetails map(ResultSet rs, StatementContext ctx) throws SQLException {
        return RegisteredUserDetails.builder()
                .userId(rs.getString("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .build();
    }
}
