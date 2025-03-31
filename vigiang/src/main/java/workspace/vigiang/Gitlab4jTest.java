package workspace.vigiang;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.*;
import org.gitlab4j.api.utils.ISO8601;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Gitlab4jTest {

    public static void main(String[] args) {
        try (GitLabApi gitLabApi = new GitLabApi("https://flngit01.cognyte.local/", "...")) {
            gitLabApi.setIgnoreCertificateErrors(true);

            List<Project> projects = gitLabApi.getProjectApi().getProjects();

            List<String> vigiangProjects = List.of(
                    "auth-service",
                    "block-service",
                    "carrier-service",
                    "config-server",
                    "dashboard-service",
                    "data-retention-service",
                    "eureka-server",
                    "event-service",
                    "interception-service",
                    "log-service",
                    "message-service",
                    "operation-service",
                    "portability-service",
                    "process-service",
                    "report-service",
                    "scheduler-service",
                    "sittel-service",
                    "system-service",
                    "tracking-service",
                    "user-service",
                    "voucher-service",
                    "warrant-service",
                    "zuul-server");

            List<Project> backendProjects = projects.stream()
                    .filter((project) -> vigiangProjects.contains(project.getName()))
                    .collect(Collectors.toList());

            List<String> names = backendProjects.stream()
                    .map(Project::getName)
                    .sorted()
                    .collect(Collectors.toList());

            names.forEach(name -> System.out.println(name));

            String frontend = "vigia_ng_app";
            Project frontendProject = projects.stream()
                    .filter((project) -> frontend.equalsIgnoreCase(project.getName()))
                    .findFirst()
                    .orElseThrow();

            List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(frontendProject.getId());

            branches.stream()
                    .map(Branch::getName)
                    .sorted()
                    .forEach(name -> System.out.println(name));

            Branch devBranch = branches.stream()
                    .filter((branch) -> "dev".equalsIgnoreCase(branch.getName()))
                    .findFirst()
                    .orElseThrow();
            System.out.println(devBranch.getName());

            RepositoryFile file = gitLabApi.getRepositoryFileApi().getFile(frontendProject.getId(), "SRC/apps/workflow/.env", "dev");
            System.out.println(file.getDecodedContentAsString() + "\n");

            file = gitLabApi.getRepositoryFileApi().getFile(frontendProject.getId(), "SRC/apps/workflow/.env", "main");
            System.out.println(file.getDecodedContentAsString());


            Date since = ISO8601.toDate("2024-11-01T00:00:00Z");
            Date until = new Date();
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits(frontendProject.getId(), "dev", since, until);
            System.out.println(commits.size());

            List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(frontendProject.getId());
            System.out.println(mergeRequests.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
