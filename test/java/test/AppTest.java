package hello;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void testGreet() {
        App app = new App();
        assertEquals("Hello, World!", app.greet("World"));
    }
}
