package com.project.orchestrate.modules.organization_module.mapper;

import com.project.orchestrate.modules.organization_module.dto.OrganizationResponse;
import com.project.orchestrate.modules.organization_module.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {

    @Mapping(target = "createdBy", source = "createdBy")
    OrganizationResponse mapToOrganizationResponse(Organization organization);
}
