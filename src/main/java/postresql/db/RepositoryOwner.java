package postresql.db;


import java.io.Serializable;

public class RepositoryOwner extends User implements Serializable {
    public RepositoryOwner(User user) {
        this(user.id, user.login);
    }
    public RepositoryOwner(long id, String login) {
        this.id = id;
        this.login = login;
    }

    @Override
    public String toString() {
        StringBuilder contributor = new StringBuilder();
        contributor.append("\n").append(getLogin());
        return contributor.toString();
    }
}
