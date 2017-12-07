package postgresql.db;

import java.io.Serializable;


public class RepositoryOwner extends User implements Serializable {
    public RepositoryOwner(long id, String login) {
        this.id = id;
        this.login = login;
    }
}
