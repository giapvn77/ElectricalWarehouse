package com.electricalwarehouse.service;

import com.electricalwarehouse.dto.request.UserRequest;
import com.electricalwarehouse.dto.response.PagedResult;
import com.electricalwarehouse.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PagedResult<UserResponse> search(String keyword, Pageable pageable);

    UserResponse get(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    void setActive(Long id, boolean active);
}
