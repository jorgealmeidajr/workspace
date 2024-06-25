package workspace.vigiang;

public class Main {

    public static void main(String[] args) {
        System.out.println("#".repeat(3 * 3));
        System.out.println("# START execution of all checkers\n");

        CheckContainers.main(new String[] { });
        CheckDatabases.main(new String[] { });
        CheckProjectsVersions.main(new String[] { });

        System.out.println("\n# END of execution of all checkers.");
        System.out.println("#".repeat(3 * 3));
    }

}
