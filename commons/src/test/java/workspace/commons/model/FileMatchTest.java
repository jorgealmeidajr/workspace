package workspace.commons.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileMatchTest {

    @Nested
    class GetContentTxt {
        @Test
        void shouldReturnFormattedStringWithSingleMatch() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  match1\n", result);
        }

        @Test
        void shouldReturnFormattedStringWithMultipleMatches() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1"),
                new FileMatch("dir2", "match2"),
                new FileMatch("dir3", "match3")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  match1\n  match2\n  match3\n", result);
        }

        @Test
        void shouldSortMatchesAlphabetically() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "zebra"),
                new FileMatch("dir2", "apple"),
                new FileMatch("dir3", "monkey")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  apple\n  monkey\n  zebra\n", result);
        }

        @Test
        void shouldRemoveDuplicateMatches() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1"),
                new FileMatch("dir2", "match1"),
                new FileMatch("dir3", "match2"),
                new FileMatch("dir4", "match1")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  match1\n  match2\n", result);
        }

        @Test
        void shouldHandleEmptyList() {
            List<FileMatch> matches = Collections.emptyList();

            String result = FileMatch.getContentTxt(matches);

            assertEquals("", result);
        }

        @Test
        void shouldHandleDuplicatesAndSort() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "charlie"),
                new FileMatch("dir2", "alpha"),
                new FileMatch("dir3", "charlie"),
                new FileMatch("dir4", "beta")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  alpha\n  beta\n  charlie\n", result);
        }

        @Test
        void shouldFormatWithTwoSpacesPrefix() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir", "test")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  test\n", result);
        }

        @Test
        void shouldHandleSpecialCharactersInMatch() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir", "match-with-dashes"),
                new FileMatch("dir", "match_with_underscores"),
                new FileMatch("dir", "match.with.dots")
            );

            String result = FileMatch.getContentTxt(matches);

            assertEquals("  match-with-dashes\n  match.with.dots\n  match_with_underscores\n", result);
        }
    }

    @Nested
    class GetContentMd {
        @Test
        void shouldReturnFormattedStringWithSingleDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  match1\n\n", result);
        }

        @Test
        void shouldReturnFormattedStringWithMultipleDirectories() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1"),
                new FileMatch("dir2", "match2"),
                new FileMatch("dir3", "match3")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  match1\n\ndir2:\n  match2\n\ndir3:\n  match3\n\n", result);
        }

        @Test
        void shouldSortDirectoriesAlphabetically() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("zebra", "match1"),
                new FileMatch("apple", "match2"),
                new FileMatch("monkey", "match3")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("apple:\n  match2\n\nmonkey:\n  match3\n\nzebra:\n  match1\n\n", result);
        }

        @Test
        void shouldGroupMatchesByDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1"),
                new FileMatch("dir1", "match2"),
                new FileMatch("dir2", "match3")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  match1\n  match2\n\ndir2:\n  match3\n\n", result);
        }

        @Test
        void shouldRemoveDuplicateMatchesWithinDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "match1"),
                new FileMatch("dir1", "match1"),
                new FileMatch("dir1", "match2")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  match1\n  match2\n\n", result);
        }

        @Test
        void shouldHandleEmptyList() {
            List<FileMatch> matches = Collections.emptyList();

            String result = FileMatch.getContentMd(matches);

            assertEquals("", result);
        }

        @Test
        void shouldSortMatchesWithinEachDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir1", "zebra"),
                new FileMatch("dir1", "apple"),
                new FileMatch("dir1", "monkey")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  apple\n  monkey\n  zebra\n\n", result);
        }

        @Test
        void shouldGroupSortAndDeduplicate() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir2", "charlie"),
                new FileMatch("dir1", "alpha"),
                new FileMatch("dir2", "alpha"),
                new FileMatch("dir1", "charlie"),
                new FileMatch("dir2", "charlie")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir1:\n  alpha\n  charlie\n\ndir2:\n  alpha\n  charlie\n\n", result);
        }

        @Test
        void shouldFormatWithColonAfterDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("testdir", "match")
            );

            String result = FileMatch.getContentMd(matches);

            assertTrue(result.contains("testdir:"), "Result should contain directory name with colon");
        }

        @Test
        void shouldFormatWithTwoSpacesPrefixBeforeMatch() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir", "match")
            );

            String result = FileMatch.getContentMd(matches);

            assertTrue(result.contains("  match"), "Result should contain matches with 2-space prefix");
        }

        @Test
        void shouldHandleSpecialCharactersInDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir-with-dashes", "match1"),
                new FileMatch("dir_with_underscores", "match2"),
                new FileMatch("dir.with.dots", "match3")
            );

            String result = FileMatch.getContentMd(matches);

            assertTrue(result.contains("dir-with-dashes:"));
            assertTrue(result.contains("dir.with.dots:"));
            assertTrue(result.contains("dir_with_underscores:"));
        }

        @Test
        void shouldHandleSpecialCharactersInMatches() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir", "match-with-dashes"),
                new FileMatch("dir", "match_with_underscores"),
                new FileMatch("dir", "match.with.dots")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("dir:\n  match-with-dashes\n  match.with.dots\n  match_with_underscores\n\n", result);
        }

        @Test
        void shouldHandleMultipleDirectoriesWithDuplicatesAndSorting() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("zebra", "delta"),
                new FileMatch("alpha", "beta"),
                new FileMatch("zebra", "alpha"),
                new FileMatch("alpha", "delta"),
                new FileMatch("alpha", "beta")
            );

            String result = FileMatch.getContentMd(matches);

            assertEquals("alpha:\n  beta\n  delta\n\nzebra:\n  alpha\n  delta\n\n", result);
        }

        @Test
        void shouldPreserveOrderWhenNoSortingNeeded() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("a", "x"),
                new FileMatch("b", "y"),
                new FileMatch("c", "z")
            );

            String result = FileMatch.getContentMd(matches);

            int indexA = result.indexOf("a:");
            int indexB = result.indexOf("b:");
            int indexC = result.indexOf("c:");

            assertTrue(indexA < indexB && indexB < indexC, "Directories should appear in alphabetical order");
        }

        @Test
        void shouldEndWithBlankLineAfterLastDirectory() {
            List<FileMatch> matches = Arrays.asList(
                new FileMatch("dir", "match")
            );

            String result = FileMatch.getContentMd(matches);

            assertTrue(result.endsWith("\n\n"), "Result should end with blank line");
        }
    }

}
