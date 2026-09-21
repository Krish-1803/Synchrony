package com.synchrony.inclusion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the full application context wires up with the in-memory test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class InclusionApplicationTests {

    @Test
    void contextLoads() {
        // The assertion is that the context above starts without error.
    }
}
