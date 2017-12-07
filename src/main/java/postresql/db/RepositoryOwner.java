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
}
