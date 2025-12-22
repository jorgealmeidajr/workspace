package workspace.vigiang;

import workspace.commons.model.FileContent;
import workspace.commons.model.XmlMyBatisMapping;
import workspace.commons.service.MappersService;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static workspace.commons.service.FileContentService.getFileContentsByExtensions;
import static workspace.commons.service.FileService.writeMd;
import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateMybatis {

    public static void main(String[] args) {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            try {
                updateMybatis(backendPath, versionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateMybatis(Path backendPath, Path versionPath) throws IOException {
        var fileContents = getFileContentsByExtensions(backendPath, List.of("xml"), List.of("commons", "target")).stream()
                .filter(f -> f.getRelativeDir().contains("\\repository\\"))
                .collect(Collectors.toList());

        updateMappers(versionPath, fileContents);
        writeMd(fileContents, Paths.get(versionPath + "\\mybatis.md"));
    }

    private static void updateMappers(Path versionPath, List<FileContent> backendFileContents) {
        try {
            var mappings = new ArrayList<XmlMyBatisMapping>();
            for (FileContent backendFileContent : backendFileContents) {
                String database = null;
                if (backendFileContent.getRelativeDir().endsWith("\\oracle")) {
                    database = "oracle";
                } else if (backendFileContent.getRelativeDir().endsWith("\\postgres")) {
                    database = "postgres";
                }

                var mapping = MappersService.getXmlMappings(backendFileContent.getContent(), database);
                mappings.add(mapping);
            }

            MappersService.writeMappers(versionPath, mappings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
