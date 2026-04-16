package com.project.orchestrate.modules.organization_module.config;

import com.project.orchestrate.modules.organization_module.model.enums.Plan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "organization")
public class OrganizationPlanProperties {

    private Map<Plan, Limit> planLimits = new EnumMap<>(Plan.class);

    public Limit getLimitOrThrow(Plan plan) {
        Limit limit = planLimits.get(plan);
        if (limit == null) {
            throw new IllegalStateException("Missing limits for plan: " + plan);
        }
        return limit;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Limit {
        private int maxProjects;
        private int maxMembers;
    }
}
