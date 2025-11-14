package workspace.commons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DatabaseCredentials {

    private String name;
    private Database database;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private boolean active;

}