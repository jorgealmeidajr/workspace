package workspace.vigiang.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Laboratory extends workspace.commons.model.Laboratory {
    private Carrier carrier;
}
