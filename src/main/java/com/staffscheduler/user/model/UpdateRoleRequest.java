package com.staffscheduler.user.model;

import com.staffscheduler.authentication.model.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateRoleRequest {

    private Role role;
    @NonNull
    private Long userId;
}
