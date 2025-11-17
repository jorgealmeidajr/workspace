package workspace.commons.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@NoArgsConstructor
@Getter
@Setter
public class DatabaseCredentials {

    private String name;
    private Database database;
    private String username;
    private String password;
    private String url;
    private boolean active;

    public static Connection getConnection(DatabaseCredentials databaseCredentials) throws SQLException {
        return DriverManager.getConnection(
                databaseCredentials.getUrl(),
                databaseCredentials.getUsername(),
                databaseCredentials.getPassword());
    }

}