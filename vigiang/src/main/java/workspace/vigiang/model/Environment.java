package workspace.vigiang.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Environment {

    private String name;
    private Carrier carrier;
    private Environment.Database database;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private String sshHost;
    private String sshUsername;
    private String sshPassword;
    private int sshPort;
    private boolean active;

    public enum Database {
        ORACLE, POSTGRES
    }

}