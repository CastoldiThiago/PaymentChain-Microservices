package com.paymentchain.notification.controller;

import com.paymentchain.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Este endpoint es solo para testear que el servicio vive y responde
    @GetMapping("/check")
    public String check() {
        return "Notification Service is UP 🚀";
    }

    // Opcional: Podrías exponer un endpoint para reenviar una notificación manual
    // si alguna vez lo necesitas.
    @PostMapping("/test")
    public ResponseEntity<String> sendTestNotification(@RequestBody String message) {
        // Aquí podrías llamar a un método de tu servicio que envíe un email real
        // notificationService.sendEmail("test@test.com", message);
        return ResponseEntity.ok("Prueba de notificación recibida: " + message);
    }
}