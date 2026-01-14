package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;

public class FileServiceTest {

    @Nested
    class IsSvgXml {
        @Test
        void testNullInput() {
            assertFalse(FileService.isSvgXml(null));
        }

        @Test
        void testValidSvgXml() {
            String svg = "<svg><circle/></svg>";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testValidSvgXmlWithAttributes() {
            String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\"><rect/></svg>";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testSvgWithWhitespace() {
            String svg = "  <svg>  </svg>  ";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testSvgCaseInsensitive() {
            assertTrue(FileService.isSvgXml("<SVG></SVG>"));
            assertTrue(FileService.isSvgXml("<Svg></Svg>"));
        }

        @Test
        void testMissingSvgOpenTag() {
            assertFalse(FileService.isSvgXml("</svg>"));
        }

        @Test
        void testMissingSvgCloseTag() {
            assertFalse(FileService.isSvgXml("<svg>"));
        }

        @Test
        void testEmptyString() {
            assertFalse(FileService.isSvgXml(""));
        }

        @Test
        void testNonSvgXml() {
            assertFalse(FileService.isSvgXml("<div></div>"));
        }

        @Test
        void testSvgWithNewlines() {
            String svg = "<svg>\n  <circle/>\n</svg>";
            assertTrue(FileService.isSvgXml(svg));
        }
    }

    @Nested
    class RightPad {
        @Test
        void testNullInput() {
            assertNull(FileService.rightPad(null, 10, " "));
        }

        @Test
        void testNullPadString() {
            assertNull(FileService.rightPad("hello", 10, null));
        }

        @Test
        void testBothNull() {
            assertNull(FileService.rightPad(null, 10, null));
        }

        @Test
        void testInputLongerThanLength() {
            assertEquals("hello", FileService.rightPad("hello", 3, " "));
        }

        @Test
        void testInputEqualToLength() {
            assertEquals("hello", FileService.rightPad("hello", 5, " "));
        }

        @Test
        void testSimplePaddingWithSpace() {
            assertEquals("hello     ", FileService.rightPad("hello", 10, " "));
        }

        @Test
        void testPaddingWithMultipleCharacters() {
            assertEquals("hello*-*-*-", FileService.rightPad("hello", 11, "*-"));
        }

        @Test
        void testPaddingWithSingleCharacter() {
            assertEquals("hello0000", FileService.rightPad("hello", 9, "0"));
        }

        @Test
        void testEmptyStringPadding() {
            assertEquals("     ", FileService.rightPad("", 5, " "));
        }

        @Test
        void testPaddingWithEmptyLength() {
            assertEquals("", FileService.rightPad("", 0, " "));
        }

        @Test
        void testLengthOneNeedsPadding() {
            assertEquals("a  ", FileService.rightPad("a", 3, " "));
        }

        @Test
        void testLargeLength() {
            String result = FileService.rightPad("x", 100, " ");
            assertEquals(100, result.length());
            assertTrue(result.startsWith("x"));
            assertTrue(result.endsWith("                                                                                                   "));
        }

        @Test
        void testPaddingWithComplexString() {
            assertEquals("helloXYZXYZ", FileService.rightPad("hello", 11, "XYZ"));
        }
    }

    @Nested
    class CalculateColumnWidth {
        @Test
        void testSingleHeaderWithShortString() {
            String[] headers = {"Name"};
            assertEquals(4, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testSingleHeaderWithLongString() {
            String[] headers = {"VeryLongHeaderName"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersWithSameLength() {
            String[] headers = {"Name", "City", "Type"};
            assertEquals(4, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersWithDifferentLengths() {
            String[] headers = {"Name", "CityName", "C"};
            assertEquals(8, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersFirstIsLongest() {
            String[] headers = {"VeryLongHeaderName", "City", "Age"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersLastIsLongest() {
            String[] headers = {"Name", "City", "VeryLongHeaderName"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersMiddleIsLongest() {
            String[] headers = {"Name", "VeryLongHeaderName", "City"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testSingleCharacterHeader() {
            String[] headers = {"A"};
            assertEquals(1, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testEmptyStringHeader() {
            String[] headers = {""};
            assertEquals(0, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleEmptyStringHeaders() {
            String[] headers = {"", "", ""};
            assertEquals(0, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testHeadersWithSpaces() {
            String[] headers = {"First Name", "Last Name", "Age"};
            assertEquals(10, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testHeadersWithSpecialCharacters() {
            String[] headers = {"Name@User", "Email#Domain", "ID-Number"};
            assertEquals(12, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testLargeNumberOfHeaders() {
            String[] headers = {"A", "BB", "CCC", "DDDD", "EEEEE", "FFFFFF"};
            assertEquals(6, FileService.calculateColumnWidth(headers));
        }
    }

    @Nested
    class WriteData {
        @Test
        void testEmptyData() {
            String[] columns = {"Name", "Age"};
            java.util.List<String[]> data = new java.util.ArrayList<>();

            String result = FileService.writeData(columns, data);

            assertEquals("", result);
        }

        @Test
        void testSingleRowSingleColumn() {
            String[] columns = {"Name"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John"});

            String result = FileService.writeData(columns, data);

            assertThat(result).startsWith("Name: John");
        }

        @Test
        void testSingleRowMultipleColumns() {
            String[] columns = {"Name", "Age", "City"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John", "25", "NYC"});

            String result = FileService.writeData(columns, data);

            String[] lines = getNonEmptyLines(result);
            assertEquals(3, lines.length);
            assertTrue(lines[0].startsWith("Name"));
            assertTrue(lines[1].startsWith("Age"));
            assertTrue(lines[2].startsWith("City"));
        }

        @Test
        void testMultipleRowsSingleColumn() {
            String[] columns = {"Name"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John"});
            data.add(new String[]{"Jane"});
            data.add(new String[]{"Bob"});

            String result = FileService.writeData(columns, data);

            String[] lines = getNonEmptyLines(result);
            assertEquals(3, lines.length);
            assertTrue(lines[0].contains("John"));
            assertTrue(lines[1].contains("Jane"));
            assertTrue(lines[2].contains("Bob"));
        }

        @Test
        void testMultipleRowsMultipleColumns() {
            String[] columns = {"Name", "Age"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John", "25"});
            data.add(new String[]{"Jane", "30"});

            String result = FileService.writeData(columns, data);

            String[] lines = getNonEmptyLines(result);
            assertEquals(4, lines.length);
            assertTrue(lines[0].contains("Name"));
            assertTrue(lines[1].contains("Age"));
            assertTrue(lines[2].contains("Name"));
            assertTrue(lines[3].contains("Age"));
        }

        String[] getNonEmptyLines(String input) {
            return java.util.Arrays.stream(input.split(System.lineSeparator()))
                    .filter(line -> !line.trim().isEmpty())
                    .toArray(String[]::new);
        }

        @Test
        void testColumnsPaddingWithDifferentLengths() {
            String[] columns = {"FirstName", "A", "City"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John", "25", "NYC"});

            String result = FileService.writeData(columns, data);

            String[] lines = getNonEmptyLines(result);
            assertTrue(lines[0].contains("FirstName:"));
            assertTrue(lines[1].contains("A        :"));
            assertTrue(lines[2].contains("City     :"));
        }

        @Test
        void testEmptyStringDataValues() {
            String[] columns = {"Name", "Age"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"", ""});

            String result = FileService.writeData(columns, data);

            String[] lines = getNonEmptyLines(result);
            assertEquals(2, lines.length);
            assertTrue(lines[0].startsWith("Name:"));
            assertTrue(lines[1].startsWith("Age :"));
        }

        @Test
        void testSpecialCharactersInData() {
            String[] columns = {"Name", "Value"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John@Doe", "!@#$%"});

            String result = FileService.writeData(columns, data);

            assertTrue(result.contains("John@Doe"));
            assertTrue(result.contains("!@#$%"));
        }

        @Test
        void testNumbersAsStringData() {
            String[] columns = {"ID", "Amount"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"1001", "9999.99"});

            String result = FileService.writeData(columns, data);

            assertTrue(result.contains("1001"));
            assertTrue(result.contains("9999.99"));
        }

        @Test
        void testWhitespaceInData() {
            String[] columns = {"Name", "Description"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"  John  ", "  A test  "});

            String result = FileService.writeData(columns, data);

            assertTrue(result.contains("  John  "));
            assertTrue(result.contains("  A test  "));
        }

        @Test
        void testLongColumnNames() {
            String[] columns = {"VeryLongFirstColumnName", "AnotherLongColumnName", "ShortName"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"Value1", "Value2", "Value3"});

            String result = FileService.writeData(columns, data);

            assertTrue(result.contains("VeryLongFirstColumnName"));
            assertTrue(result.contains("AnotherLongColumnName"));
            assertTrue(result.contains("ShortName"));
        }

        @Test
        void testFormattingStructure() {
            String[] columns = {"Name", "Age"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John", "25"});

            String result = FileService.writeData(columns, data);

            // Verify format includes column name, colon, and value
            assertTrue(result.contains(": "));
            assertTrue(result.contains("John"));
            assertTrue(result.contains("25"));
        }

        @Test
        void testManyRows() {
            String[] columns = {"ID", "Name"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                data.add(new String[]{String.valueOf(i), "User" + i});
            }

            String result = FileService.writeData(columns, data);
            String[] lines = getNonEmptyLines(result);

            assertEquals(200, lines.length); // 2 columns per row * 100 rows
        }

        @Test
        void testReturnTypeNotNull() {
            String[] columns = {"Name"};
            java.util.List<String[]> data = new java.util.ArrayList<>();
            data.add(new String[]{"John"});

            String result = FileService.writeData(columns, data);

            assertNotNull(result);
        }
    }

    @Nested
    class ReplaceLines {
        @Test
        void testBasicReplacement() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN MARKER",
                    "old content",
                    "END MARKER",
                    "line 5"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList(
                    "new content 1",
                    "new content 2"
            );

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN MARKER", "END MARKER", replacementLines);

            assertEquals(6, result.size());
            assertEquals("line 1", result.get(0));
            assertEquals("BEGIN MARKER", result.get(1));
            assertEquals("new content 1", result.get(2));
            assertEquals("new content 2", result.get(3));
            assertEquals("END MARKER", result.get(4));
            assertEquals("line 5", result.get(5));
        }

        @Test
        void testReplaceWithEmptyContent() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN",
                    "content to remove",
                    "END",
                    "line 5"
            );
            java.util.List<String> replacementLines = new java.util.ArrayList<>();

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(4, result.size());
            assertEquals("line 1", result.get(0));
            assertEquals("BEGIN", result.get(1));
            assertEquals("END", result.get(2));
            assertEquals("line 5", result.get(3));
        }

        @Test
        void testReplaceMultipleLinesInMiddle() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN",
                    "old1",
                    "old2",
                    "old3",
                    "END",
                    "line 7"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList(
                    "new1",
                    "new2"
            );

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(6, result.size());
            assertEquals("line 1", result.get(0));
            assertEquals("BEGIN", result.get(1));
            assertEquals("new1", result.get(2));
            assertEquals("new2", result.get(3));
            assertEquals("END", result.get(4));
            assertEquals("line 7", result.get(5));
        }

        @Test
        void testMarkerWithPartialMatch() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "// BEGIN REPLACEMENT",
                    "old",
                    "// END REPLACEMENT",
                    "line 5"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(5, result.size());
            assertEquals("// BEGIN REPLACEMENT", result.get(1));
            assertEquals("new", result.get(2));
            assertEquals("// END REPLACEMENT", result.get(3));
        }

        @Test
        void testBeginMarkerNotFound() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "old content",
                    "END MARKER",
                    "line 4"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> FileService.replaceLines(lines, "BEGIN MARKER", "END MARKER", replacementLines)
            );
            assertTrue(exception.getMessage().contains("Begin marker not found"));
        }

        @Test
        void testEndMarkerNotFound() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN MARKER",
                    "old content",
                    "line 4"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> FileService.replaceLines(lines, "BEGIN MARKER", "END MARKER", replacementLines)
            );
            assertTrue(exception.getMessage().contains("End marker not found after begin"));
        }

        @Test
        void testEndMarkerBeforeBeginMarker() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "END MARKER",
                    "BEGIN MARKER",
                    "old content"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> FileService.replaceLines(lines, "BEGIN MARKER", "END MARKER", replacementLines)
            );
            assertTrue(exception.getMessage().contains("End marker not found after begin"));
        }

        @Test
        void testConsecutiveMarkers() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN",
                    "END",
                    "line 4"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("replacement");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(5, result.size());
            assertEquals("BEGIN", result.get(1));
            assertEquals("replacement", result.get(2));
            assertEquals("END", result.get(3));
        }

        @Test
        void testReplaceWithMoreLinesThanOriginal() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN",
                    "old",
                    "END",
                    "line 5"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList(
                    "new1",
                    "new2",
                    "new3",
                    "new4",
                    "new5"
            );

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(9, result.size());
            assertEquals("line 1", result.get(0));
            assertEquals("BEGIN", result.get(1));
            assertEquals("new1", result.get(2));
            assertEquals("new5", result.get(6));
            assertEquals("END", result.get(7));
            assertEquals("line 5", result.get(8));
        }

        @Test
        void testReplaceAtBeginningOfFile() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "BEGIN",
                    "old content",
                    "END",
                    "line 4"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(4, result.size());
            assertEquals("BEGIN", result.get(0));
            assertEquals("new", result.get(1));
            assertEquals("END", result.get(2));
        }

        @Test
        void testReplaceAtEndOfFile() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "BEGIN",
                    "old content",
                    "END"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(4, result.size());
            assertEquals("line 1", result.get(0));
            assertEquals("BEGIN", result.get(1));
            assertEquals("new", result.get(2));
            assertEquals("END", result.get(3));
        }

        @Test
        void testMultipleReplacements() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "BEGIN",
                    "old",
                    "END"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList(
                    "new1",
                    "new2",
                    "new3"
            );

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(5, result.size());
            assertEquals("BEGIN", result.get(0));
            assertEquals("new1", result.get(1));
            assertEquals("new2", result.get(2));
            assertEquals("new3", result.get(3));
            assertEquals("END", result.get(4));
        }

        @Test
        void testMarkerIsCaseSensitive() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "BEGIN",
                    "old",
                    "END"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> FileService.replaceLines(lines, "begin", "end", replacementLines)
            );
            assertTrue(exception.getMessage().contains("Begin marker not found"));
        }

        @Test
        void testSingleContentLine() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "BEGIN",
                    "content",
                    "END"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN", "END", replacementLines);

            assertEquals(3, result.size());
            assertEquals("BEGIN", result.get(0));
            assertEquals("new", result.get(1));
            assertEquals("END", result.get(2));
        }

        @Test
        void testWithSpecialCharactersInMarkers() {
            java.util.List<String> lines = java.util.Arrays.asList(
                    "line 1",
                    "<!-- BEGIN BLOCK -->",
                    "old content",
                    "<!-- END BLOCK -->",
                    "line 5"
            );
            java.util.List<String> replacementLines = java.util.Arrays.asList("new content");

            java.util.List<String> result = FileService.replaceLines(lines, "BEGIN BLOCK", "END BLOCK", replacementLines);

            assertEquals(5, result.size());
            assertEquals("<!-- BEGIN BLOCK -->", result.get(1));
            assertEquals("new content", result.get(2));
            assertEquals("<!-- END BLOCK -->", result.get(3));
        }
    }

}
