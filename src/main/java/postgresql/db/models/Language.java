package postgresql.db.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
public class Language implements Serializable {
    protected static final long serialVersionUID = 1L;
    @NotNull
    @Getter
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
