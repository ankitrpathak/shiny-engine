package com.carwash.adminservice.client;

import com.carwash.adminservice.dto.UserDto;
import com.carwash.adminservice.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/users")
    List<UserDto> getAllUsers(@RequestHeader("Authorization") String token);

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PutMapping("/api/users/{id}/block")
    UserDto blockUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PutMapping("/api/users/{id}/activate")
    UserDto activateUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PutMapping("/api/users/{id}/promote-admin")
    UserDto promoteToAdmin(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PutMapping("/api/users/{id}/promote-washer")
    UserDto promoteToWasher(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PutMapping("/api/users/{id}/demote-customer")
    UserDto demoteToCustomer(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/users/admin/summary")
    UserSummaryDto getUserSummary(@RequestHeader("Authorization") String token);
}
