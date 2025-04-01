package workspace.vigiang.checkers;

import workspace.vigiang.dao.DbSchemaDAO;
import workspace.vigiang.model.Environment;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CheckSchemas {

    public static void main(String[] args) {
        System.out.println("## START checking all database schemas\n");
        try {
            execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

    private static void execute() throws IOException {
        List<Environment> environments = EnvironmentService.getEnvironments();
        ExecutorService executorService = Executors.newFixedThreadPool(environments.size());
        List<Callable<String>> callableTasks = new ArrayList<>();

        for (Environment env : environments) {
            Callable<String> callableTask = getCallableTask(env);
            callableTasks.add(callableTask);
        }

        try {
            List<Future<String>> futures = executorService.invokeAll(callableTasks);

            for (Future<String> future : futures) {
                String result = future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        try {
            if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private static Callable<String> getCallableTask(Environment env) {
        return () -> {
            DbSchemaDAO dao = EnvironmentService.getDbSchemaDAO(env);
            System.out.println(env.getName() + ":");

            try {

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println();

            TimeUnit.MILLISECONDS.sleep(1000);
            return "Task execution completed";
        };
    }

}
