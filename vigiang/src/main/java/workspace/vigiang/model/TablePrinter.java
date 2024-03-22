package workspace.vigiang.model;

import java.util.List;

public class TablePrinter {

    public static String printRow(String[] values, int[] columnWidths) {
        String result = "|";
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            int width = columnWidths[i];
            result += String.format(" %-" + width + "s |", value) ;
        }
        return result;
    }

    public static int[] calculateColumnWidths(List<String[]> data) {
        var columnsLength = data.get(0).length;
        int[] columnWidths = new int[columnsLength];

        for (String[] row : data) {
            for (int i = 0; i < columnsLength; i++) {
                int length = row[i].length();
                if (length > columnWidths[i]) {
                    columnWidths[i] = length;
                }
            }
        }

        return columnWidths;
    }

    public static int[] calculateColumnWidths(String[] headers, List<String[]> data) {
        int columns = headers.length;
        int[] columnWidths = new int[columns];

        for (String[] row : data) {
            for (int i = 0; i < columns; i++) {
                int length = row[i].length();
                if (length > columnWidths[i]) {
                    columnWidths[i] = length;
                }
            }
        }

        for (int i = 0; i < columns; i++) {
            int headerLength = headers[i].length();
            if (headerLength > columnWidths[i]) {
                columnWidths[i] = headerLength;
            }
        }

        return columnWidths;
    }

    public static void printTable(String[] headers, List<String[]> data) {
        int[] columnWidths = calculateColumnWidths(headers, data);

        printHorizontalLine(columnWidths);
        System.out.println(printRow(headers, columnWidths));
        printHorizontalLine(columnWidths);

        for (String[] row : data) {
            System.out.println(printRow(row, columnWidths));
        }

        printHorizontalLine(columnWidths);
    }

    public static String printHorizontalLine(int[] columnWidths) {
        String result = "| ";
        for (int width : columnWidths) {
            result += "-".repeat(width) + " | ";
        }
        return result.trim();
    }

}
