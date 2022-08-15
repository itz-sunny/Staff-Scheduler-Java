package com.staffscheduler.user.controller;

import com.staffscheduler.apidoc.ErrorResponse;
import com.staffscheduler.aspect.LogController;
import com.staffscheduler.exception.DuplicateUserException;
import com.staffscheduler.user.model.RegisterDto;
import com.staffscheduler.user.model.User;
import com.staffscheduler.user.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecuredAnnotationRule;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import javax.validation.Valid;
import java.util.Optional;

@LogController
@Secured(SecuredAnnotationRule.IS_ANONYMOUS)
@Controller("app/v1/register")
@Tag(name = "Register", description = "The User APIs provide functionality to register a new User. One can create a new user.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegisterController {

    @Inject
    private UserService userService;

    @Post
    @Operation(summary = "Creates a new user in database",
            description = "Takes name, email, password to create a new user in the database")
    @ApiResponse(responseCode = "201", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "409", description = "Returned if a user already exists with the provided email")
    public Flowable<MutableHttpResponse<?>> register(@Body @Valid RegisterDto registerDto) {
        return Flowable.fromCallable(() -> {
            try {
                Optional<User> optionalUser = userService.register(registerDto);
                return optionalUser.map(HttpResponse::created).orElse(HttpResponse.serverError());
            } catch (DuplicateUserException due) {
                return HttpResponse.status(HttpStatus.CONFLICT).body(due.getMessage());
            } catch (Exception e) {
                return HttpResponse.serverError();
            }
        }).subscribeOn(Schedulers.io());
    }

}
