package postresql.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User implements Serializable {
    protected long id;
    protected String login;

    @Override
    public String toString() {
        StringBuilder contributor = new StringBuilder(login)
                    .append(" with id: ")
                    .append(id);
        return contributor.toString();
    }
}
