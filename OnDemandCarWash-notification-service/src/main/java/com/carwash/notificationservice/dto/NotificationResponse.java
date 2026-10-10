package com.carwash.notificationservice.dto;

import com.carwash.notificationservice.entity.NotificationStatus;
import com.carwash.notificationservice.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private Long referenceId;
    private LocalDateTime createdAt;
}
