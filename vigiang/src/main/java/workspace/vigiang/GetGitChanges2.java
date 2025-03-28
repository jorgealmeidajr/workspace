package workspace.vigiang;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Project;

import java.util.List;
import java.util.stream.Collectors;

public class GetGitChanges2 {

    public static void main(String[] args) {
        final var BRANCH_A = "version-1.3.4";
        final var BRANCH_B = "main";
        final var CARRIER_TARGET = "SURF";

        try(GitLabApi gitLabApi = new GitLabApi("https://flngit01.cognyte.local/", "...")) {
            gitLabApi.setIgnoreCertificateErrors(true);

            List<Project> projects = gitLabApi.getProjectApi().getProjects();

            Project frontendProject = projects.stream()
                    .filter((project) -> "vigia_ng_app".equalsIgnoreCase(project.getName()))
                    .findFirst()
                    .orElseThrow();

            List<Branch> frontendBranches = gitLabApi.getRepositoryApi().getBranches(frontendProject.getId());

            List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(frontendProject.getId());

            Branch frontendBranchA = frontendBranches.stream()
                    .filter((branch) -> BRANCH_A.equalsIgnoreCase(branch.getName()))
                    .findFirst()
                    .orElseThrow();

            List<MergeRequest> mergesBranchA = mergeRequests.stream()
                    .filter((mr) -> BRANCH_A.equalsIgnoreCase(mr.getTargetBranch()))
                    .collect(Collectors.toList());

            Branch frontendBranchB = frontendBranches.stream()
                    .filter((branch) -> BRANCH_B.equalsIgnoreCase(branch.getName()))
                    .findFirst()
                    .orElseThrow();

            List<MergeRequest> mergesBranchB = mergeRequests.stream()
                    .filter((mr) -> BRANCH_B.equalsIgnoreCase(mr.getTargetBranch()))
                    .collect(Collectors.toList());

            // get database issues
            String url = "https://flngit01.cognyte.local/dev/vigiang/database/" + CARRIER_TARGET.toLowerCase();
            Project databaseProject = projects.stream()
                    .filter((project) -> url.equals(project.getWebUrl()))
                    .findFirst()
                    .orElseThrow();

            List<Issue> issues = gitLabApi.getIssuesApi().getIssues(databaseProject.getId());
            for (Issue issue : issues) {
                System.out.println(issue.getTitle());
                System.out.println(issue.getWebUrl());
                System.out.println(issue.getDescription());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
