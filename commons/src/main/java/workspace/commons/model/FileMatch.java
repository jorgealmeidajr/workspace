package workspace.commons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
public class FileMatch {

    private final String relativeDir;
    private final String match;

    public FileMatch(String relativeDir, String match) {
        if (relativeDir == null || relativeDir.isEmpty()) {
            throw new IllegalArgumentException("relativeDir cannot be empty or null");
        }
        if (match == null || match.isEmpty()) {
            throw new IllegalArgumentException("match cannot be empty or null");
        }
        this.relativeDir = relativeDir;
        this.match = match;
    }

    public static String getContentTxt(List<FileMatch> matches) {
        List<String> sortedMatches = matches.stream()
                .map(FileMatch::getMatch)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String result = "";
        for (String match : sortedMatches) {
            result += "  " + match + "\n";
        }
        return result;
    }

    public static String getContentMd(List<FileMatch> matches) {
        Map<String, List<FileMatch>> grouped = matches.stream()
                .collect(Collectors.groupingBy(FileMatch::getRelativeDir));

        List<String> dirs = new ArrayList<>(grouped.keySet());
        dirs.sort(String::compareTo);

        String result = "";
        for (String dir : dirs) {
            result += dir + ":\n";

            List<FileMatch> sortedUnique = grouped.get(dir).stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(FileMatch::getMatch, fm -> fm, (a, b) -> a, LinkedHashMap::new),
                            m -> m.values().stream()
                                    .sorted(Comparator.comparing(FileMatch::getMatch))
                                    .collect(Collectors.toList())
                    ));

            for (FileMatch fm : sortedUnique) {
                result += "  " + fm.getMatch() + "\n";
            }
            result += "\n";
        }
        return result;
    }

}
