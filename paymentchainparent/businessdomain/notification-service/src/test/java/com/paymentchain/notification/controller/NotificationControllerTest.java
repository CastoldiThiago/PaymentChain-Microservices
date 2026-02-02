package com.paymentchain.notification.controller;

import com.paymentchain.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setup() {
        NotificationController controller = new NotificationController();
        // inject via reflection is unnecessary because controller methods don't use the service in these tests
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void check_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/notification/check"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification Service is UP")));

        verifyNoInteractions(notificationService);
    }

    @Test
    void postTest_shouldEchoBack() throws Exception {
        mockMvc.perform(post("/notification/test")
                .contentType(MediaType.TEXT_PLAIN)
                .content("hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Prueba de notificación recibida: hello"));
    }
}
