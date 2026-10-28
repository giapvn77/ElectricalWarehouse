package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.request.UserRequest;
import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.UserResponse;
import com.electricalwarehouse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<PagedResult<UserResponse>> search(@RequestParam(required = false) String keyword,
                                                         @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(userService.search(keyword, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<Void> setActive(@PathVariable Long id, @RequestParam boolean value) {
        userService.setActive(id, value);
        return ApiResponse.ok();
    }
}
