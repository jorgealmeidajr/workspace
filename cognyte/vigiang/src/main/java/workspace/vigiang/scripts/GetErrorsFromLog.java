package workspace.vigiang.scripts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GetErrorsFromLog {

    public static void main(String[] args) throws Exception {
        // TODO: refactor these parameters
        var logDateStr = "2025-07-05";
        var logPathStr = "C:\\Users\\jjunior\\Downloads\\tim1";

        Path logPath = Paths.get(logPathStr + "\\vigia-ng." + logDateStr + ".log");
        Path inputPath = Paths.get(logPath.toString(), "vigia-ng." + logDateStr + ".log");
        Path outputPath = Paths.get(logPath.toString(), "errors.log");

        List<String> lines = getNonEmptyLines(inputPath);
        List<String> errors = getErrors(lines);

        String content = getContent(errors);
        Files.writeString(outputPath, content, StandardCharsets.UTF_8);
    }

    private static String getContent(List<String> errors) {
        List<String> errorsToIgnore = getErrorsToIgnore();

        String content = "";
        for (String error : errors) {
            if (errorsToIgnore.stream().anyMatch(error::contains)) continue;
            content += error + "\n";
            content += "---------------\n";
        }
        return content;
    }

    private static List<String> getErrorsToIgnore() {
        // TODO: group these error codes
        return List.of(
                "LOGIN_INVALID",
                "INVALID_USER_PASSWORD",
                "VIGIAV3.USUARIO_SENHA_INVALIDO",
                "USER_INACTIVE",
                "VIGIAV3.SENHA_INVALIDA",
                "EXPIRED_PASSWORD",
                "VIGIAV3.NAO_PODE_REUTILIZAR_SENHA",
                "DUPLICATE_PASSWORD",
                "USER_LOCKED",
                "CAPTCHA_CHECK",
                "USER_FORGOT_PASSWORD_NOT_FOUND",
                "CONCURRENCY_LOGIN",

                "listReceivedNotIncludedInterceptions;",
                "listReceivedInterceptionsWithErrorByDay;",
                "listInterceptsWithErrorCarrier;",
                "listSentInterceptionsWithErrorByDay;",

                "org.apache.catalina.valves.ErrorReportValve.invoke",
                "DefaultSecurityFilterChain;Creating filter chain: any request",
                "SecurityCredentialsConfig;Error while logout",
                "JwtUsernameAndPasswordAuthenticationFilter;Delegating to authentication failure handler",
                "JwtTokenAuthenticationFilter;Error authenticating, invalid token.",
                "TOTPAuthController;TOTP verification failed",
                "ErrorFilter;Zuul failure detected: Filter threw Exception"
        );
    }

    private static List<String> getErrors(List<String> lines) {
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String lineOriginal = lines.get(i).trim();
            String lineEdited = lineOriginal.toLowerCase();

            if (lineEdited.contains("erro") || lineEdited.contains("exception")
                    || lineEdited.contains("fail") || lineEdited.contains("falha")) {
                int start = Math.max(0, i - 5);
                int end = Math.min(i + 6, lines.size());
                StringBuilder error = new StringBuilder();
                for (int j = start; j < end; j++) {
                    error.append(lines.get(j)).append("\n");
                }
                errors.add(error.toString());
                i += 6;
            }
        }
        return errors;
    }

    public static List<String> getNonEmptyLines(Path filePath) throws Exception {
        return Files.readString(filePath)
                .lines()
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

}
