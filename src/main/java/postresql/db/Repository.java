package postresql.db;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;


@Getter
public class Repository implements Serializable {
    private long id;
   @Setter
    private RepositoryOwner owner;
    private String name;
    private String description;
    private Language language;
    @Setter
    private int starsCount;
    @Setter
    private int commitsCount;
    @Setter
    private Contributor[] contributors;

    public Repository(long id, RepositoryOwner owner, String name, String description, Language language) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.language = language;
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
                    if (fieldValue instanceof String || (fieldValue instanceof Integer && (Integer) fieldValue != 0))
                        repo.append("\n").append(fieldName)
                                .append(": ").append(fieldValue);
                    else if (fieldValue instanceof Language)
                        repo.append("\n").append("language: ")
                                .append(((Language) fieldValue).getName());
                }
            }
        }
        catch (IllegalAccessException e) {
            System.out.println("Some fields weren't retrieved!");
        }
        return repo.toString();
    }
}
