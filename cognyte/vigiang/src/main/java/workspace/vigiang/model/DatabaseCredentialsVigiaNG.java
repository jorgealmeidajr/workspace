package workspace.vigiang.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import workspace.commons.model.DatabaseCredentials;

@NoArgsConstructor
@Getter
@Setter
public class DatabaseCredentialsVigiaNG extends DatabaseCredentials {
    private Carrier carrier;
}