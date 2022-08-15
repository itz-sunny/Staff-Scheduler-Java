package com.staffscheduler.schedule.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class ScheduleRequest extends ScheduleBase {
    @NotNull
    private Long userId;
}
