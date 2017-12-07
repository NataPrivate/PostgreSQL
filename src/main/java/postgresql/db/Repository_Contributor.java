package postgresql.db;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class Repository_Contributor {
    private long repositoryId;
    private long contributorId;
    int commits_count;
}
