package workspace.commons.service;

import workspace.commons.model.FileContent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FileService {

    public static void updateLocalFiles(String name, String fileName, String[] columns, List<String[]> data, Path outputFolderPath) throws IOException {
        var result =
            "# " + name + " | " + fileName + "\n" +
            "```\n" +
            writeData(columns, data) +
            "```\n";

        Path outputPath = Paths.get(outputFolderPath + "\\" + fileName + ".md");

        writeString(outputPath, result);
    }

    static String writeData(String[] columns, List<String[]> data) {
        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(columns);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < columns.length; i++) {
                var column = rightPad(columns[i], columnWidth, " ");
                line += column + ": " + row[i] + System.lineSeparator();
            }
            finalLines.add(line);
        }

        return String.join(System.lineSeparator(), finalLines);
    }

    static int calculateColumnWidth(String[] headers) {
        int maxValue = headers[0].length();
        for (String header : headers) {
            int headerLength = header.length();
            if (headerLength > maxValue) {
                maxValue = headerLength;
            }
        }
        return maxValue;
    }

    static String rightPad(String input, int length, String padStr) {
        if(input == null || padStr == null){
            return null;
        }

        if(input.length() >= length){
            return input;
        }

        int padLength = length - input.length();

        StringBuilder paddedString = new StringBuilder();
        paddedString.append(input);

        // Calculate how many times to repeat padStr to ensure we have enough characters
        int repetitions = (padLength / padStr.length()) + 1;
        String padding = padStr.repeat(repetitions);
        // Truncate to exact length needed
        paddedString.append(padding.substring(0, padLength));

        return paddedString.toString();
    }

    public static boolean isSvgXml(String logo) {
        if (logo == null) return false;
        String trimmedLogo = logo.trim().toLowerCase();
        return trimmedLogo.contains("<svg") && trimmedLogo.contains("</svg>");
    }

    public static void replaceRegion(Path file, String beginMarker, String endMarker, List<String> replacementLines) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int beginIdx = -1, endIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(beginMarker)) {
                beginIdx = i;
                break;
            }
        }
        if (beginIdx == -1) throw new IllegalStateException("Begin marker not found: " + beginMarker);
        for (int i = beginIdx + 1; i < lines.size(); i++) {
            if (lines.get(i).contains(endMarker)) {
                endIdx = i;
                break;
            }
        }
        if (endIdx == -1) throw new IllegalStateException("End marker not found after begin: " + endMarker);

        List<String> out = new ArrayList<>();
        out.addAll(lines.subList(0, beginIdx + 1));
        out.addAll(replacementLines);
        out.addAll(lines.subList(endIdx, lines.size()));

        Files.write(file, out, StandardCharsets.UTF_8);
    }

    public static void writeMd(List<FileContent> fileContents, Path outputPath) throws IOException {
        String resultTxt = "";

        fileContents.sort(Comparator.comparing(FileContent::getFullName));

        for (FileContent fileContent : fileContents) {
            String input = fileContent.getContent().trim();

            resultTxt += "# " + fileContent.getFullName() + ":\n";
            resultTxt += "```\n";
            resultTxt += input.trim() + "\n";
            resultTxt += "```\n\n";
        }

        String result = resultTxt.trim() + "\n";
        writeString(outputPath, result);
    }

    public static void writeString(Path outputPath, String result) throws IOException {
        if (!Files.exists(outputPath) && result.isEmpty()) {
            return; // ignoring file
        }

        var initialFileContent = "";
        if (Files.exists(outputPath)) {
            initialFileContent = new String(Files.readAllBytes(outputPath));

            if (!initialFileContent.equals(result)) {
                System.out.println("updating file: " + outputPath);
                Files.writeString(outputPath, result, StandardCharsets.UTF_8);
            }

        } else {
            System.out.println("creating file: " + outputPath);
            Files.writeString(outputPath, result, StandardCharsets.UTF_8);
        }
    }

}
