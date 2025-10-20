package org.example.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.service.AquariumNotificationService;
import org.example.backend.global.rsdata.RsData;
import org.springframework.web.bind.annotation.*;

// 실제 서비스에서 사용안되는 테스트를 위한 코드
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationTestController {
    
    private final AquariumNotificationService notificationService;
    
    /**
     * 특정 어항에 대해 테스트 알림 발송
     * @param aquariumId 어항 ID
     * @return 발송 결과
     */
    @PostMapping("/test/{aquariumId}")
    public RsData<Void> sendTestNotification(@PathVariable Long aquariumId) {
        notificationService.sendTestNotification(aquariumId);
        return new RsData<>("200", "테스트 알림이 발송되었습니다.");
    }
    
    /**
     * 모든 알림 대상 어항에 대해 수동으로 알림 발송
     * @return 발송 결과
     */
    @PostMapping("/send-all")
    public RsData<Void> sendAllNotifications() {
        notificationService.sendAllNotifications();
        return new RsData<>("200", "모든 알림이 발송되었습니다.");
    }
}
