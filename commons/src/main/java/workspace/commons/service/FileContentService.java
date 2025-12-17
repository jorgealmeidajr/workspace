package workspace.commons.service;

import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;

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

public class FileContentService {

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
                        String fullName = Paths.get(relativeDir.toString(), p.getFileName().toString()).toString();
                        try {
                            return new FileContent(fullName, relativeDir.toString(), p.getFileName().toString(), Files.readString(p));
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

}
