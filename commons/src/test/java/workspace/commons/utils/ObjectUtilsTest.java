package workspace.commons.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectUtilsTest {

    @Test
    public void testRequireNonBlank_ValidValue() {
        String result = ObjectUtils.requireNonBlank("validValue", "Value should not be blank");
        assertEquals("validValue", result);
    }

    @Test
    public void testRequireNonBlank_NullValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectUtils.requireNonBlank(null, "Value cannot be null");
        });
    }

    @Test
    public void testRequireNonBlank_EmptyString() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectUtils.requireNonBlank("", "Value cannot be empty");
        });
    }

    @Test
    public void testRequireNonBlank_BlankString() {
        assertThrows(IllegalArgumentException.class, () -> {
            ObjectUtils.requireNonBlank("   ", "Value cannot be blank");
        });
    }

    @Test
    public void testRequireNonBlank_ExceptionMessage() {
        String expectedMessage = "Custom error message";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ObjectUtils.requireNonBlank(null, expectedMessage);
        });
        assertEquals(expectedMessage, exception.getMessage());
    }

}
