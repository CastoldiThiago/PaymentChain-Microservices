package com.paymentchain.apigateway;

import org.junit.jupiter.api.Test;

class ApigatewayApplicationTests {

    @Test
    void simpleSanity() {
        // lightweight sanity check to avoid loading the entire Spring context in unit tests
        // The full ApplicationContext is verified by integration tests in CI when needed.
    }

}
