package com.project.orchestrate.modules.organization_module.mapper;

import com.project.orchestrate.modules.user_module.dto.UserSummary;
import com.project.orchestrate.modules.user_module.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SummaryMapper {
    default Record UserSummary(User user) {
        if (user == null) return null;
        return new UserSummary(user.getId(), user.getName(), user.getEmail());
    }
}
