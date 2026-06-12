package workspace.vigiang;

import workspace.commons.model.FileContent;
import workspace.commons.model.MyBatisMappings;
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
import static workspace.commons.service.FileService.writeString;
import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateMybatis {

    public static void main(String[] args) {
        var WORK_DIR = EnvironmentService.getWorkVigiaDir();

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            String versionTitle = "VERSION: " + version;
            System.out.println(versionTitle);

            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            try {
                updateMybatis(backendPath, versionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("-".repeat(versionTitle.length()));
        }
    }

    private static void updateMybatis(Path backendPath, Path versionPath) throws IOException {
        var fileContents = getFileContentsByExtensions(backendPath, List.of("xml"), List.of("commons", "target")).stream()
                .filter(f -> f.getRelativeDir().contains("\\repository\\"))
                .collect(Collectors.toList());

        updateMappers(versionPath, fileContents);

        var oracleFiles = fileContents.stream()
                .filter(f -> f.getRelativeDir().endsWith("\\oracle"))
                .collect(Collectors.toList());
        writeMd(oracleFiles, Paths.get(versionPath + "\\back\\mybatis.oracle.md"));

        var postgresFiles = fileContents.stream()
                .filter(f -> f.getRelativeDir().endsWith("\\postgres"))
                .collect(Collectors.toList());
        writeMd(postgresFiles, Paths.get(versionPath + "\\back\\mybatis.pg.md"));
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

                var relativeDirLower = backendFileContent.getRelativeDir();
                var splitPath = relativeDirLower.split("\\\\");
                var project = splitPath[1];

                XmlMyBatisMapping mapping = MappersService.getXmlMappings(backendFileContent.getContent(), database, project);
                mappings.add(mapping);
            }

            var myBatisMappings = new MyBatisMappings(mappings);

            writeMappers(versionPath, myBatisMappings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeMappers(Path versionPath, MyBatisMappings myBatisMappings) throws IOException {
        String resultTxt = myBatisMappings.getMappersTxt();
        writeContentToFile(resultTxt, versionPath, "\\mappers.txt");

        String resultMd = myBatisMappings.getMappersMd();
        writeContentToFile(resultMd, versionPath, "\\mappers.md");
    }

    private static void writeContentToFile(String result, Path filePath, String fileName) throws IOException {
        Path outputPath = Paths.get(filePath + fileName);
        writeString(outputPath, result);
    }

}
