package com.staffscheduler.user.model;

import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@Introspected
public class UserUpdateRequest {
    @NonNull
    private Long userId;
    @NotEmpty
    private String name;
    @Email
    private String email;
    @Size(min=8,max=20)
    @Nullable
    private String password;
}
