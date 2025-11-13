package workspace.vigiang.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import workspace.commons.model.Database;

@NoArgsConstructor
@Getter
@Setter
public class DatabaseCredentials {

    private String name;
    private Carrier carrier;
    private Database database;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private boolean active;

}