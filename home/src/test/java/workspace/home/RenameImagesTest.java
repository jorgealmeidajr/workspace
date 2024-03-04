package workspace.home;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class RenameImagesTest {

    @Test
    void getNewNameTest() {
        LocalDateTime nowDate = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0, 0);
        assertEquals("000101_000.jpg", RenameImages.getNewName(0, "jpg", nowDate));

        nowDate = LocalDateTime.of(2001, Month.JANUARY, 10, 0, 0, 0);
        assertEquals("010110_010.jpg", RenameImages.getNewName(10, "jpg", nowDate));

        nowDate = LocalDateTime.of(2010, Month.JANUARY, 10, 0, 0, 0);
        assertEquals("100110_010.jpg", RenameImages.getNewName(10, "jpg", nowDate));
    }

}
