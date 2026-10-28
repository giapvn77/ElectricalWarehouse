package com.electricalwarehouse.controller;

import com.electricalwarehouse.entity.User;
import com.electricalwarehouse.entity.UserRole;
import com.electricalwarehouse.entity.enums.RoleCode;
import com.electricalwarehouse.repository.RoleRepository;
import com.electricalwarehouse.repository.UserRepository;
import com.electricalwarehouse.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Security chain over HTTP: login issues a JWT, protected endpoints require it, permissions are enforced. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void loginThenCallProtectedEndpoints() throws Exception {
        String username = "nvbh-" + UUID.randomUUID().toString().substring(0, 6);
        User user = new User();
        user.setUsername(username);
        user.setFullName("Nhân viên bán hàng");
        user.setPasswordHash(passwordEncoder.encode("Secret#123"));
        userRepository.save(user);
        userRoleRepository.save(new UserRole(user, roleRepository.findByCode(RoleCode.NVBH).orElseThrow()));

        mvc.perform(get("/api/dashboard/summary")).andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Secret#123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.roles[0]").value("NVBH"))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).get("data");
        String bearer = "Bearer " + data.get("accessToken").asText();

        mvc.perform(get("/api/auth/me").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username));
        mvc.perform(get("/api/dashboard/pending-approvals").header("Authorization", bearer))
                .andExpect(status().isOk());

        // NVBH has no USER_MANAGE permission.
        mvc.perform(get("/api/users").header("Authorization", bearer)).andExpect(status().isForbidden());

        // Refresh rotates the token: the old refresh token stops working.
        String refreshJson = "{\"refreshToken\":\"" + data.get("refreshToken").asText() + "\"}";
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshJson))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshJson))
                .andExpect(status().isUnauthorized());
    }
}
