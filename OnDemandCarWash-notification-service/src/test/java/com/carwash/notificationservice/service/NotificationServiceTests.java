package com.carwash.notificationservice.service;

import com.carwash.notificationservice.dto.NotificationRequest;
import com.carwash.notificationservice.dto.NotificationResponse;
import com.carwash.notificationservice.entity.Notification;
import com.carwash.notificationservice.entity.NotificationStatus;
import com.carwash.notificationservice.entity.NotificationType;
import com.carwash.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .userEmail("customer@carwash.com")
                .title("Booking Placed")
                .message("Your booking #1 is placed.")
                .type(NotificationType.BOOKING_CREATED)
                .status(NotificationStatus.UNREAD)
                .referenceId(1L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateNotification_Success() {
        NotificationRequest request = NotificationRequest.builder()
                .userId(101L)
                .userEmail("customer@carwash.com")
                .title("Booking Placed")
                .message("Your booking #1 is placed.")
                .type(NotificationType.BOOKING_CREATED)
                .referenceId(1L)
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(sampleNotification);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Booking Placed", response.getTitle());
        assertEquals(NotificationStatus.UNREAD, response.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications_ReturnsList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(101L))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> list = notificationService.getUserNotifications(101L);

        assertEquals(1, list.size());
        assertEquals("customer@carwash.com", list.get(0).getUserEmail());
    }

    @Test
    void testGetUnreadNotifications_ReturnsUnreadOnly() {
        when(notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(101L, NotificationStatus.UNREAD))
                .thenReturn(List.of(sampleNotification));

        List<NotificationResponse> list = notificationService.getUnreadNotifications(101L);

        assertEquals(1, list.size());
        assertEquals(NotificationStatus.UNREAD, list.get(0).getStatus());
    }

    @Test
    void testGetUnreadCount_ReturnsCount() {
        when(notificationRepository.countByUserIdAndStatus(101L, NotificationStatus.UNREAD))
                .thenReturn(3L);

        long count = notificationService.getUnreadCount(101L);

        assertEquals(3L, count);
    }

    @Test
    void testMarkAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L);

        assertNotNull(response);
        assertEquals(NotificationStatus.READ, response.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAllAsRead_Success() {
        when(notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(101L, NotificationStatus.UNREAD))
                .thenReturn(List.of(sampleNotification));
        when(notificationRepository.saveAll(any())).thenReturn(List.of(sampleNotification));

        notificationService.markAllAsRead(101L);

        assertEquals(NotificationStatus.READ, sampleNotification.getStatus());
        verify(notificationRepository, times(1)).saveAll(any());
    }
}
