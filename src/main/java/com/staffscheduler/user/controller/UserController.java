package com.staffscheduler.user.controller;

import com.staffscheduler.apidoc.ErrorResponse;
import com.staffscheduler.aspect.LogController;
import com.staffscheduler.user.service.UserService;
import com.staffscheduler.user.model.SortedUser;
import com.staffscheduler.user.model.UpdateRoleRequest;
import com.staffscheduler.user.model.UserUpdateRequest;
import io.micronaut.http.HttpResponse;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@LogController
@Controller("app/v1/user")
@Secured(SecuredAnnotationRule.IS_AUTHENTICATED)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User", description = "The User APIs provide functionalities to operate on User Objects. One can perform operations like fetch, update etc. on users.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    private UserService userService;

    @Get("/fetch/sorted")
    @Secured("ADMIN")
    @Operation(summary = "Fetch all users sorted by accumulated shift hours in descending order during a period",
            description = "Set the start date and end date. start date must not be greater than end date. Also difference between start date and end date must be within a year")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SortedUser.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<?>> fetchAllUsersSorted(@QueryValue Date startDate, @QueryValue Date endDate) {
        return Flowable.fromCallable(() -> {
            List<SortedUser> users = userService.fetchAllUsersSorted(startDate, endDate);
            return users.isEmpty() ? HttpResponse.notFound() : HttpResponse.ok(users);
        }).subscribeOn(Schedulers.io());
    }

    @Put("/role/update")
    @Secured("ADMIN")
    @Operation(summary = "Updates the role of a user",
            description = "Set role to required and user id")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> updateRole(@Body @Valid UpdateRoleRequest request) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(userService.updateRoleByUserId(request));
        }).subscribeOn(Schedulers.io());
    }

    @Put("/update")
    @Secured("ADMIN")
    @Operation(summary = "Updates the user",
            description = "Set name, email, password to be updated. All fields other than user id is optional. User will be updated")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> updateUser(@Body @Valid UserUpdateRequest request) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(userService.updateUserById(request));
        }).subscribeOn(Schedulers.io());
    }

    @Delete("/delete/{userId}")
    @Secured("ADMIN")
    @Operation(summary = "Deletes the user",
            description = "Pass userId to be deleted as path variable and user will be deleted")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> deleteUser(@PathVariable long userId) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(userService.deleteUserById(userId));
        }).subscribeOn(Schedulers.io());
    }
}
