package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Feature {

    private Integer code;
    private String id;
    private String status;
    private String description;

    public String[] toArray() {
        return new String[] { id, status, description };
    }

}
