package com.staffscheduler.schedule.controller;

import com.staffscheduler.apidoc.ErrorResponse;
import com.staffscheduler.authentication.model.Role;
import com.staffscheduler.schedule.model.Schedule;
import com.staffscheduler.schedule.model.ScheduleBase;
import com.staffscheduler.schedule.model.ScheduleRequest;
import com.staffscheduler.schedule.service.ScheduleService;
import com.staffscheduler.user.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
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

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller("app/v1/schedule")
@Secured(SecuredAnnotationRule.IS_AUTHENTICATED)
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User", description = "The User APIs provide functionalities to operate on User Objects. One can perform operations like fetch, update etc. on users.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduleController {

    @Inject
    private ScheduleService scheduleService;

    @Inject
    private UserService userService;

    @Post("/create")
    @Secured("ADMIN")
    @Operation(summary = "Creates a new schedule",
            description = "Set startDate, endDate, userId, workDate and shiftHours to create a new schedule entry in the database")
    @ApiResponse(responseCode = "201", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Schedule.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Schedule>> createSchedule(@Body ScheduleRequest scheduleRequest) {
        return Flowable.fromCallable(() -> {
            Optional<Schedule> scheduleOptional = scheduleService.createSchedule(scheduleRequest);
            return scheduleOptional.map(HttpResponse::created).orElse(HttpResponse.serverError());
        }).subscribeOn(Schedulers.io());
    }

    @Get("/fetch/self")
    @Operation(summary = "Fetch all schedules in given duration for the authenticated user",
            description = "Set the start date and end date. start date must not be greater than end date. Also difference between start date and end date must be within a year")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<List<Schedule>>> fetch(@QueryValue String startDate, @QueryValue String endDate, Principal principal) {
        return Flowable.fromCallable(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            List<Schedule> schedules = scheduleService.fetchScheduleByUserId(Long.parseLong(principal.getName()), start, end);
            return schedules.isEmpty() ? HttpResponse.notFound(schedules) : HttpResponse.ok(schedules);
        }).subscribeOn(Schedulers.io());
    }

    @Get("/fetch/{userId}")
    @Operation(summary = "Fetch all schedules in given duration for the user with provided userId",
            description = "Set the start date and end date. start date must not be greater than end date. Also difference between start date and end date must be within a year. User id must be present")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<List<Schedule>>> fetch(@PathVariable long userId, @QueryValue String startDate, @QueryValue String endDate, Authentication authentication) {
        return Flowable.fromCallable(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            List<Schedule> schedules = new ArrayList<>();
            Role role = userService.fetchRoleByUserId(userId);
            if (role.equals(Role.ADMIN) && authentication.getRoles().contains(Role.STAFF.name()))
                return HttpResponse.notFound(schedules);
            schedules = scheduleService.fetchScheduleByUserId(userId, start, end);
            return schedules.isEmpty() ? HttpResponse.notFound(schedules) : HttpResponse.ok(schedules);
        }).subscribeOn(Schedulers.io());
    }

    @Get("/fetch/all")
    @Operation(summary = "Fetch all schedules in given duration for all users",
            description = "Set the start date and end date. start date must not be greater than end date. Also difference between start date and end date must be within a year")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<List<Schedule>>> fetchAll(@QueryValue String startDate, @QueryValue String endDate, Authentication authentication) {
        return Flowable.fromCallable(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            List<Schedule> schedules = scheduleService.fetchAllSchedules(start, end);
            if (authentication.getRoles().contains(Role.STAFF.name()))
                schedules = schedules.stream()
                        .filter(schedule -> !Objects.isNull(schedule) && !Objects.isNull(schedule.getUser()) && Role.ADMIN.name().equals(schedule.getUser().getRole()))
                        .collect(Collectors.toList());
            return schedules.isEmpty() ? HttpResponse.notFound(schedules) : HttpResponse.ok(schedules);
        }).subscribeOn(Schedulers.io());
    }

    @Delete("/delete/{scheduleId}")
    @Secured("ADMIN")
    @Operation(summary = "Delete schedule by id",
            description = "schedule id must be present. will delete schedule corresponding to provided id")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> delete(@PathVariable long scheduleId) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(scheduleService.deleteScheduleById(scheduleId));
        }).subscribeOn(Schedulers.io());
    }

    @Delete("/delete/all/{userId}")
    @Secured("ADMIN")
    @Operation(summary = "Delete schedules for user with provided user id",
            description = "user id must be present. will delete schedules corresponding to provided user id")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> deleteAll(@PathVariable long userId) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(scheduleService.deleteAllSchedulesByUserId(userId));
        }).subscribeOn(Schedulers.io());
    }

    @Put("/update")
    @Secured("ADMIN")
    @Operation(summary = "Updates schedule",
            description = "Update one or more fields (userId, workDate, shiftHours). UserId must be present")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "500", description = "Some error occurred. Please try after sometime.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "API not found")
    @ApiResponse(responseCode = "401", description = "Returned if request is submitted by an anonymous user or if the access token is invalid e.g. expired")
    @ApiResponse(responseCode = "403", description = "Returned when user does not have permissions to access/modify this resource")
    public Flowable<MutableHttpResponse<Boolean>> update(ScheduleBase request) {
        return Flowable.fromCallable(() -> {
            return HttpResponse.ok(scheduleService.updateScheduleById(request));
        }).subscribeOn(Schedulers.io());
    }

}
