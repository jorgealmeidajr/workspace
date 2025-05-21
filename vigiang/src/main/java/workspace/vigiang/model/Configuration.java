package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Configuration {

    private String carrierId;
    private String id;
    private String description;
    private String value;

    public String[] toArray() {
        return new String[] { carrierId, id, description, value };
    }

}
