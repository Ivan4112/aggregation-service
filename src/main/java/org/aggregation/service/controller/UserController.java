package org.aggregation.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.aggregation.service.model.User;
import org.aggregation.service.model.UserFilter;
import org.aggregation.service.service.UserAggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Aggregated users from all configured data sources")
public class UserController {

    private final UserAggregationService userAggregationService;

    public UserController(UserAggregationService userAggregationService) {
        this.userAggregationService = userAggregationService;
    }

    @Operation(
            summary = "Get aggregated users",
            description = "Selects and aggregates users from every configured data source. "
                    + "Optional query parameters filter the result and are pushed down into each source query.")
    @ApiResponse(
            responseCode = "200",
            description = "Aggregated list of users",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class))))
    @GetMapping
    public List<User> getUsers(
            @Parameter(description = "Filter by user id") @RequestParam(required = false) String id,
            @Parameter(description = "Filter by username") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by surname") @RequestParam(required = false) String surname) {
        return userAggregationService.getUsers(new UserFilter(id, username, name, surname));
    }
}
