package workspace.commons.utils;

public class StringUtils {

    public static String getValueAfterDot(String name) {
        int dotIndex = name.indexOf(".");
        return dotIndex >= 0 ? name.substring(dotIndex + 1) : name;
    }

    public static String getValueBeforeDot(String name) {
        int dotIndex = name.indexOf(".");
        return dotIndex >= 0 ? name.substring(0, dotIndex) : name;
    }

}
