package workspace.vigiang.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads environment specific settings from a '.env' file.
 *
 * <p>The file is looked up by walking up from the current working directory, so the same '.env'
 * works whether the JVM starts in the module directory or in the workspace root. Values can also
 * be provided as real environment variables, which take precedence over the file. There are no
 * defaults: when a setting is missing, an {@link IllegalStateException} is thrown.
 *
 * <p>The parser is intentionally minimal (no dependency): blank lines and '#' comments are skipped,
 * the line is split on the first '=', and optional surrounding quotes are removed. Backslashes are
 * kept as-is, so Windows paths such as 'C:\work' do not need escaping.
 */
public class ConfigService {

    private static final String ENV_FILE_NAME = ".env";

    private static final String WORK_PATH_KEY = "WORK_PATH";
    private static final String USER_BASHRC_PATH_KEY = "USER_BASHRC_PATH";

    private static final Map<String, String> VALUES = loadEnvFile();

    private ConfigService() {
    }

    public static String getWorkPath() {
        return getRequired(WORK_PATH_KEY);
    }

    public static String getUserBashrcPath() {
        return getRequired(USER_BASHRC_PATH_KEY);
    }

    private static String getRequired(String key) {
        String fromEnvironment = System.getenv(key);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment.trim();
        }

        String fromFile = VALUES.get(key);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }

        throw new IllegalStateException("'" + key + "' is not set."
                + " Define it as an environment variable or in a '" + ENV_FILE_NAME + "' file"
                + " (see '.env.example' in the workspace root).");
    }

    private static Map<String, String> loadEnvFile() {
        Path envPath = findEnvFile();
        if (envPath == null) {
            System.out.println("no '" + ENV_FILE_NAME + "' file found,"
                    + " expecting the settings as environment variables");
            return Collections.emptyMap();
        }

        System.out.println("loading configuration from: " + envPath);
        try {
            List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
            return parse(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read the file: " + envPath, e);
        }
    }

    private static Path findEnvFile() {
        Path directory = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        while (directory != null) {
            Path candidate = directory.resolve(ENV_FILE_NAME);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            directory = directory.getParent();
        }

        return null;
    }

    static Map<String, String> parse(List<String> lines) {
        Map<String, String> values = new LinkedHashMap<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex <= 0) continue;

            String key = trimmed.substring(0, separatorIndex).trim();
            String value = unquote(trimmed.substring(separatorIndex + 1).trim());

            if (key.isEmpty()) continue;

            values.put(key, value);
        }

        return values;
    }

    private static String unquote(String value) {
        if (value.length() < 2) return value;

        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");

        if (doubleQuoted || singleQuoted) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

}

