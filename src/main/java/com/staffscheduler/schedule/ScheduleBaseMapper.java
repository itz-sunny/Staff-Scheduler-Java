package com.staffscheduler.schedule;

import com.staffscheduler.schedule.model.ScheduleBase;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ScheduleBaseMapper implements RowMapper<ScheduleBase> {


    @Override
    public ScheduleBase map(ResultSet rs, StatementContext ctx) throws SQLException {
        ScheduleBase schedule = new ScheduleBase();
        schedule.setScheduleId(rs.getLong("id"));
        schedule.setShiftHours(rs.getByte("shift_hours"));
        schedule.setWorkDate(rs.getDate("work_date"));
        return schedule;
    }
}
