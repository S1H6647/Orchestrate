package com.project.orchestrate.modules.auth_module.mapper;

import com.project.orchestrate.modules.auth_module.dto.LoginResponse;
import com.project.orchestrate.modules.auth_module.dto.MeResponse;
import com.project.orchestrate.modules.auth_module.dto.RegisterResponse;
import org.mapstruct.Mapping;
import com.project.orchestrate.modules.user_module.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    LoginResponse mapLoginResponse(User user, String accessToken, String refreshToken);

    RegisterResponse mapRegisterResponse(User user);

    @Mapping(target = "systemRole", expression = "java(user.getSystemRole() != null ? user.getSystemRole().name() : null)")
    MeResponse mapMeResponse(User user);
}
