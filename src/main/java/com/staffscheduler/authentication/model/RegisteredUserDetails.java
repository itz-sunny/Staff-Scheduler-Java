package com.staffscheduler.authentication.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisteredUserDetails{

    private String userId;
    private String name;
    private String email;
    private String password;
}
