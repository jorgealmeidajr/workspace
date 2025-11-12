package workspace.vigiang.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Laboratory {

    private String name;
    private Carrier carrier;
    private String alias;
    private String sshHost;
    private String sshUsername;
    private String sshPassword;
    private int sshPort;
    private boolean active;

}
