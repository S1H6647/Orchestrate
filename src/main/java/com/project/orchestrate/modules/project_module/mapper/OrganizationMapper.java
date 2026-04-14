package com.project.orchestrate.modules.project_module.mapper;

import com.project.orchestrate.modules.project_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.project_module.model.Organization;
import com.project.orchestrate.modules.user_module.dto.UserSummary;
import com.project.orchestrate.modules.user_module.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "createdBy", source = "createdBy")
    OrganizationResponse mapToOrganizationResponse(Organization organization);

    default UserSummary mapToUserSummary(User user) {
        if (user == null) return null;
        return new UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
