package workspace.vigiang;

public class Main {

    public static void main(String[] args) {
        System.out.println("##################");
        System.out.println("START execution of all checking for vigiang project...");
        System.out.println("##################\n");

        CheckContainers.main(new String[] { });
        CheckFeatures.main(new String[] { });
        CheckProjectsVersions.main(new String[] { });

        System.out.println("\n##################");
        System.out.println("END of execution of all checking.");
        System.out.println("##################");
    }

}
