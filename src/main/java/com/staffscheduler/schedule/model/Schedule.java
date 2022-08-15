package com.staffscheduler.schedule.model;

import com.staffscheduler.user.model.User;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@ToString
public class Schedule extends ScheduleBase {
    @NotNull
    @Valid
    private Long scheduleId;
    private User user;
    private Date createdAt;
    private Date updatedAt;
    private Boolean deleted;
}
