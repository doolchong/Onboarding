package com.example.onboarding.user.service;

import com.example.onboarding.common.dto.AuthUser;
import com.example.onboarding.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    public UserResponse getUser(AuthUser authUser) {
        return new UserResponse(authUser);
    }
}
