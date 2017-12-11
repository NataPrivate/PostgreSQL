package postgresql.db.models;

import lombok.Getter;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;


@Getter
public class Contributor extends User implements Serializable {
    @Range(min = 1)
    private int commitsCount;

    public Contributor(long id, String login, int commitsCount) {
        this.id = id;
        this.login = login;
        this.commitsCount = commitsCount;
    }

    @Override
    public String toString() {
        StringBuilder contributor = new StringBuilder(login == null ? "Unknown" : login)
                                .append(" with ")
                                .append(commitsCount)
                                .append(" commits");
        return contributor.toString();
    }
}
