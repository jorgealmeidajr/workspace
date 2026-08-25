package workspace.vigiang.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LaboratoryRef {

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Carrier carrier;

    private String host;

}

