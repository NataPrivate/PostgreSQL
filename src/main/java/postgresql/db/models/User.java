package postgresql.db.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotNull;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User implements Serializable {
    protected static final long serialVersionUID = 1L;
    @Range(min = 1)
    protected long id;
    @NotNull
    protected String login;

    @Override
    public String toString() {
        StringBuilder contributor = new StringBuilder(login == null ? "Unknown" : login)
                    .append(" with id: ")
                    .append(id);
        return contributor.toString();
    }
}
