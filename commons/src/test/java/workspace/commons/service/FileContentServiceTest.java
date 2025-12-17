package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class FileContentServiceTest {

    @Nested
    class GetMatches {
        @Test
        void testSinglePatternSingleMatch() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(1, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME") && m.getRelativeDir().equals("dir1")));
        }

        @Test
        void testMultipleMatchesSameFile() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') getConfiguration('APP_VERSION')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME") && m.getRelativeDir().equals("dir1")));
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_VERSION") && m.getRelativeDir().equals("dir1")));
        }

        @Test
        void testMultipleFilesMultipleMatches() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME')"),
                new FileContent("file2.txt", "dir2", "file2.txt", "getConfiguration('APP_VERSION')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME") && m.getRelativeDir().equals("dir1")));
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_VERSION") && m.getRelativeDir().equals("dir2")));
        }

        @Test
        void testIgnoreList() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') getConfiguration('IGNORE_ME')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of("IGNORE_ME");

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(1, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME")));
            assertFalse(result.stream().anyMatch(m -> m.getMatch().equals("IGNORE_ME")));
        }

        @Test
        void testMultiplePatterns() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') ifFeature('FEATURE_X')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]"),
                Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME")));
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("FEATURE_X")));
        }

        @Test
        void testNoMatches() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "some random content")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(0, result.size());
        }

        @Test
        void testEmptyFileContents() {
            List<FileContent> fileContents = List.of();
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(0, result.size());
        }

        @Test
        void testEmptyPatterns() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME')")
            );
            List<Pattern> patterns = List.of();
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(0, result.size());
        }

        @Test
        void testDuplicateMatches() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') getConfiguration('APP_NAME')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(1, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_NAME")));
        }

        @Test
        void testComplexPattern() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "LIST_USERS CREATE_USERS CHANGE_USERS")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("((LIST_|CREATE_|CHANGE_)[A-Z_]*)")
            );
            List<String> ignore = List.of();

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("LIST_USERS")));
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("CREATE_USERS")));
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("CHANGE_USERS")));
        }

        @Test
        void testAllIgnored() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') getConfiguration('APP_VERSION')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of("APP_NAME", "APP_VERSION");

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(0, result.size());
        }

        @Test
        void testPartiallyIgnored() {
            List<FileContent> fileContents = List.of(
                new FileContent("file1.txt", "dir1", "file1.txt", "getConfiguration('APP_NAME') getConfiguration('APP_VERSION') getConfiguration('APP_DEBUG')")
            );
            List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
            );
            List<String> ignore = List.of("APP_NAME", "APP_VERSION");

            List<FileMatch> result = FileContentService.getMatches(fileContents, patterns, ignore);

            assertEquals(1, result.size());
            assertTrue(result.stream().anyMatch(m -> m.getMatch().equals("APP_DEBUG")));
        }
    }

}
