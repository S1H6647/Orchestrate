package com.project.orchestrate.modules.project_module.mapper;

import com.project.orchestrate.modules.project_module.dto.OrganizationMemberResponse;
import com.project.orchestrate.modules.project_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.project_module.model.Organization;
import com.project.orchestrate.modules.project_module.model.OrganizationMember;
import com.project.orchestrate.modules.user_module.model.enums.AccountStatus;
import com.project.orchestrate.modules.user_module.model.enums.MemberStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "createdBy", source = "createdBy")
    OrganizationResponse mapToOrganizationResponse(Organization organization);

    OrganizationMemberResponse mapToOrganizationMemberResponse(OrganizationMember organizationMember);

    @ValueMappings({
            @ValueMapping(source = "ACTIVE", target = "ACTIVE"),
            @ValueMapping(source = "PENDING_VERIFICATION", target = "INVITED"),
            @ValueMapping(source = "INACTIVE", target = "REMOVED"),
            @ValueMapping(source = "SUSPENDED", target = "REMOVED")
    })
    MemberStatus map(AccountStatus status);
}
