package workspace.commons.service;

import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileService {

    public static void updateLocalFiles(String name, String fileName, String[] columns, List<String[]> data, Path outputFolderPath) throws IOException {
        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(columns);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < columns.length; i++) {
                var column = rightPad(columns[i], columnWidth, " ");
                line += column + ": " + row[i] + "\n";
            }
            finalLines.add(line);
        }

        var newFileContent =
            "# " + name + " | " + fileName + "\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path finalFilePath = Paths.get(outputFolderPath + "\\" + fileName + ".md");

        var initialFileContent = "";
        if (Files.exists(finalFilePath)) {
            initialFileContent = new String(Files.readAllBytes(finalFilePath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + finalFilePath);
            Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static int calculateColumnWidth(String[] headers) {
        int maxValue = headers[0].length();
        for (String header : headers) {
            int headerLength = header.length();
            if (headerLength > maxValue) {
                maxValue = headerLength;
            }
        }
        return maxValue;
    }

    private static String rightPad(String input, int length, String padStr) {
        if(input == null || padStr == null){
            return null;
        }

        if(input.length() >= length){
            return input;
        }

        int padLength = length - input.length();

        StringBuilder paddedString = new StringBuilder();
        paddedString.append(input);
        paddedString.append(padStr.repeat(padLength));

        return paddedString.toString();
    }

    public static boolean isSvgXml(String logo) {
        if (logo == null) return false;
        String trimmedLogo = logo.trim().toLowerCase();
        return trimmedLogo.contains("<svg") && trimmedLogo.contains("</svg>");
    }

    public static List<FileContent> getFileContentsByExtensions(Path dirPath, List<String> extensions, List<String> ignoreDirs) {
        List<FileContent> fileContents = List.of();
        try (var stream = Files.walk(dirPath)) {
            fileContents = stream
                    .filter(p -> Files.isRegularFile(p) &&
                            extensions.stream().anyMatch(ext -> p.toString().endsWith(ext)) &&
                            ignoreDirs.stream().noneMatch(dir -> p.toString().contains("\\" + dir + "\\")))
                    .map(p -> {
                        Path parent = p.getParent();
                        Path relativeDir = (parent == null) ? Paths.get("") : dirPath.relativize(parent);
                        try {
                            return new FileContent(relativeDir.toString(), p.getFileName().toString(), Files.readString(p));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContents;
    }

    public static List<FileMatch> getMatches(List<FileContent> fileContents, List<Pattern> patterns, List<String> ignore) {
        Set<FileMatch> matchesSet = new LinkedHashSet<>();

        for (FileContent fileContent : fileContents) {
            String input = fileContent.getContent().trim();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(input);

                while (matcher.find()) {
                    var matchStr = matcher.group(1);
                    if (ignore.contains(matchStr)) continue;

                    var match = new FileMatch(fileContent.getRelativeDir(), matchStr);
                    matchesSet.add(match);
                }
            }
        }

        return new ArrayList<>(matchesSet);
    }

}
