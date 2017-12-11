package postgresql.db.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;


@Getter
public class Repository implements Serializable {
    static final long serialVersionUID = 1L;
    @Range(min = 1)
    private long id;
    @NotNull
    private RepositoryOwner owner;
    private String name;
    private String description;
    private Language language;
    @Range(min = 0)
    private int starsCount;
    @Range(min = 0)
    private int commitsCount;
    @Setter
    private Contributor[] contributors;

    public Repository(long id, RepositoryOwner owner, String name, String description, Language language, int starsCount, int commitsCount) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.language = language;
        this.starsCount = starsCount;
        this.commitsCount = commitsCount;
    }

    @Override
    public String toString() {
        StringBuilder repo = new StringBuilder();
        try {
            String fieldName;
            Object fieldValue;
            for (Field field : this.getClass().getDeclaredFields()) {
                fieldName = field.getName();
                fieldValue = field.get(this);
                if (fieldValue != null) {
                    if (fieldValue instanceof String || fieldValue instanceof Integer)
                        repo.append("\n").append(fieldName).append(": ").append(fieldValue);
                    else if (fieldValue instanceof Language)
                        repo.append("\n").append("language: ").append((fieldValue).toString());
                    else if (fieldValue instanceof RepositoryOwner)
                        repo.append("\n").append("owner: ").append(((RepositoryOwner) fieldValue).getLogin());
                }
            }
        }
        catch (IllegalAccessException e) {
            System.out.println("Some fields weren't retrieved!");
        }
        return repo.toString();
    }
}
