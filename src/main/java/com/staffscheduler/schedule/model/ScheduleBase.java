package com.staffscheduler.schedule.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ScheduleBase {
    private Long scheduleId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;
    private Byte shiftHours;

}
