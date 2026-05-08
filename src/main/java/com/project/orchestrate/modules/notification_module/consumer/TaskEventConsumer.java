package com.project.orchestrate.modules.notification_module.consumer;

import com.project.orchestrate.common.exception.ResourceNotFoundException;
import com.project.orchestrate.modules.notification_module.dto.GenericTaskEvent;
import com.project.orchestrate.modules.notification_module.model.Notification;
import com.project.orchestrate.modules.notification_module.repository.NotificationRepository;
import com.project.orchestrate.modules.notification_module.service.EmailService;
import com.project.orchestrate.modules.project_module.model.Project;
import com.project.orchestrate.modules.project_module.model.ProjectMember;
import com.project.orchestrate.modules.project_module.repository.ProjectMemberRepository;
import com.project.orchestrate.modules.project_module.repository.ProjectRepository;
import com.project.orchestrate.modules.websocket_module.service.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rabbit.enabled", havingValue = "true")
public class TaskEventConsumer {

    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;
    private final EmailService emailService;
    private final ProjectRepository projectRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @RabbitListener(queues = "${rabbit.queue:orchestrate.queue}")
    public void consume(GenericTaskEvent event) {
        log.info("Received event: {}", event.eventType());

        Project project = projectRepository.findByIdWithOrganization(event.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<ProjectMember> projectMembers = projectMemberRepository.findAllByProjectIdWithUser(event.projectId())
                .stream()
                .filter(member -> !member.getUser().getId().equals(event.userId()))
                .toList();

        final String TASK_URL = String.format("%s/organizations/%s/projects/%s/tasks/%s",
                baseUrl,
                project.getOrganization().getSlug(),
                project.getSlug(),
                event.taskId()
        );

        for (ProjectMember member : projectMembers) {
            var user = member.getUser();
            Notification notificationPayload = Notification.builder()
                    .recipientId(user.getId())
                    .type(event.eventType())
                    .content(event.content())
                    .isRead(false)
                    .createdAt(event.timestamp())
                    .build();

            notificationRepository.save(notificationPayload);

            if (presenceService.isOnline(user.getId())) {
                messagingTemplate.convertAndSendToUser(
                        user.getId().toString(),
                        "/queue/notifications",
                        notificationPayload
                );
            } else {
                emailService.sendTaskNotificationEmail(
                        user.getEmail(),
                        user.getName(),
                        event.name(),
                        project.getName(),
                        event.taskTitle(),
                        event.eventType().toReadable(),
                        event.content(),
                        TASK_URL
                );
            }
        }

        log.info("Notification sent to: {} member(s)", (long) projectMembers.size());
    }
}
