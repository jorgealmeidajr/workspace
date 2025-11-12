package workspace.vigiang.service;

import workspace.vigiang.model.DatabaseCredentials;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileService {

    public static void updateLocalFiles(DatabaseCredentials databaseCredentials, String fileName, String[] columns, List<String[]> data) throws IOException {
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
            "# " + databaseCredentials.getName() + " | " + fileName + "\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentials);
        Path finalFilePath = Paths.get(databaseDataPath + "\\" + fileName + ".md");

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

    public static void writeLogo(Path logosPath, String[] carrier) throws IOException {
        int carrierId = Integer.parseInt(carrier[0]);
        String carrierCode = String.format("%02d", carrierId);
        String carrierName = Arrays.stream(carrier[1].trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
        carrierName = carrierName.trim().toUpperCase().replaceAll("\\s+", "_");
        String logoName = carrierCode + "-" + carrierName;

        String logo = carrier[14];
        if (logo == null || logo.trim().isEmpty()) return;

        if (isSvgXml(logo)) {
            Path logoPath = Paths.get(logosPath + "\\" + logoName + ".svg");
            Files.writeString(logoPath, logo, StandardCharsets.UTF_8);
        } else {
            System.out.println("Logo is not in SVG format=" + logo);
        }
    }

    private static boolean isSvgXml(String logo) {
        if (logo == null) return false;
        String trimmedLogo = logo.trim().toLowerCase();
        return trimmedLogo.contains("<svg") && trimmedLogo.contains("</svg>");
    }

}
