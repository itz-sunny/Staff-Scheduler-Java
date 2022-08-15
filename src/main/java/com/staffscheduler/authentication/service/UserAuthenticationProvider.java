package com.staffscheduler.authentication.service;

import com.staffscheduler.authentication.model.RegisteredUserDetails;
import com.staffscheduler.authentication.model.Role;
import com.staffscheduler.authentication.repository.AuthenticationRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserAuthenticationProvider implements AuthenticationProvider {

    @Inject
    private CredentialsValidatorService credentialsValidatorService;

    @Inject
    private AuthenticationRepository authenticationRepository;

    private final static Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationProvider.class);

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.fromCallable(() -> {
            try {

                String email = (String) authenticationRequest.getIdentity();
                String password = (String) authenticationRequest.getSecret();
                Optional<RegisteredUserDetails> optionalRegisteredUserDetails = credentialsValidatorService.validate(email, password);

                if (optionalRegisteredUserDetails.isPresent()) {
                    Optional<Role> optionalRole = authenticationRepository.fetchRole(optionalRegisteredUserDetails.get().getUserId());
                    Map<String, Object> map = new HashMap<>();
                    return AuthenticationResponse.success(
                            optionalRegisteredUserDetails.get().getUserId(),
                            Collections.singletonList(optionalRole.orElse(Role.STAFF).name())
                    );
                }
                return new AuthenticationFailed();
            } catch (AuthenticationException e) {
                LOGGER.error("Exception while authentication user with email: {}", authenticationRequest.getIdentity(), e);
                throw new AuthenticationException();
            }
        }).subscribeOn(Schedulers.io());
    }
}
