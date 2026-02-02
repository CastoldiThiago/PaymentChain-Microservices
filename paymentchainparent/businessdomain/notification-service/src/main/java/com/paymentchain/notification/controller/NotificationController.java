package com.paymentchain.notification.controller;

import com.paymentchain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification", description = "Notification service endpoints (email notifications)")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Este endpoint es solo para testear que el servicio vive y responde
    @Operation(summary = "Health check for Notification Service", description = "Simple endpoint to verify the notification service is running")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is up", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/check")
    public String check() {
        return "Notification Service is UP 🚀";
    }

    // Opcional: Podrías exponer un endpoint para reenviar una notificación manual
    // si alguna vez lo necesitas.
    @Operation(summary = "Send a test notification (echo)", description = "Endpoint for testing notifications manually. The message will be returned in the response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test notification accepted", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class)))
    })
    @PostMapping(value = "/test", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendTestNotification(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Plain text message to echo back", required = true, content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string"))) @RequestBody String message) {
        // Aquí podrías llamar a un método de tu servicio que envíe un email real
        // notificationService.sendEmail("test@test.com", message);
        return ResponseEntity.ok("Prueba de notificación recibida: " + message);
    }
}