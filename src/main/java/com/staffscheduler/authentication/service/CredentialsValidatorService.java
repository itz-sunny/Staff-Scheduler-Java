package com.staffscheduler.authentication.service;

import com.staffscheduler.authentication.model.RegisteredUserDetails;
import com.staffscheduler.authentication.repository.AuthenticationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.validation.constraints.Email;
import java.util.Optional;

@Singleton
public class CredentialsValidatorService {

    @Inject
    private AuthenticationRepository authenticationRepository;

    @Inject
    private BCryptPasswordEncoderService bCryptPasswordEncoderService;

    public Optional<RegisteredUserDetails> validate(@Email String email, String password) {
        Optional<RegisteredUserDetails> optionalRegisteredUserDetails = authenticationRepository.fetchRegisteredUserDetails(email);

        if (optionalRegisteredUserDetails.isPresent() && bCryptPasswordEncoderService.matches(password, optionalRegisteredUserDetails.get().getPassword())) {
            return optionalRegisteredUserDetails;
        }

        return Optional.empty();
    }
}
