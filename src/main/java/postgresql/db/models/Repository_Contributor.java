package postgresql.db.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;


@AllArgsConstructor
@Getter
public class Repository_Contributor {
    @Range(min = 1)
    private long repositoryId;
    @Range(min = 1)
    private long contributorId;
    @Range(min = 1)
    int commits_count;
}
