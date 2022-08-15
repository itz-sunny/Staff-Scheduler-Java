package com.staffscheduler.user.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SortedUser {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private Date createdAt;
    private Date updatedAt;
    private Integer totalShiftHours;
}
