package com.staffscheduler.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
public class User {
    @NotNull
    private Long userId;
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotBlank
    private String role;
    private Boolean deleted;
    private Date createdAt;
    private Date updatedAt;
}
