package postgresql.db;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
public class Language implements Serializable {
    @Getter
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
