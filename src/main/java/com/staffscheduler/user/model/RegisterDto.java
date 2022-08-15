package com.staffscheduler.user.model;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
public class RegisterDto {
    @NotBlank
    private String name;
    @Email
    @NotNull
    private String email;
    @NotNull
    @Size(min=8,max=20)
    private String password;
}
