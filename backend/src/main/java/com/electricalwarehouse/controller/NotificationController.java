package com.electricalwarehouse.controller;

import com.electricalwarehouse.dto.response.ApiResponse;
import com.electricalwarehouse.dto.response.NotificationResponse;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PagedResult<NotificationResponse>> mine(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(notificationService.getMine(pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.countUnread());
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok();
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.ok();
    }
}
