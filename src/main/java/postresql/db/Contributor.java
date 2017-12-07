package postresql.db;

import lombok.Getter;

import java.io.Serializable;


public class Contributor extends User implements Serializable {
    @Getter
    private int commitsCount;

    public Contributor(User user, int commitsCount) {
        this(user.id, user.login, commitsCount);
    }
    public Contributor(long id, String login, int commitsCount) {
        this.id = id;
        this.login = login;
        this.commitsCount = commitsCount;
    }

    @Override
    public String toString() {
        StringBuilder contributor = new StringBuilder();
        contributor.append("\n").append(getLogin())
                                .append(" with ")
                                .append(commitsCount)
                                .append(" commits");
        return contributor.toString();
    }
}
