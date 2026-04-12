package com.project.orchestrate.modules.auth_module.mapper;

import com.project.orchestrate.modules.auth_module.dto.LoginResponse;
import com.project.orchestrate.modules.auth_module.dto.RegisterResponse;
import com.project.orchestrate.modules.user_module.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    LoginResponse mapLoginResponse(User user, String accessToken, String refreshToken);

    RegisterResponse mapRegisterResponse(User user);
}
