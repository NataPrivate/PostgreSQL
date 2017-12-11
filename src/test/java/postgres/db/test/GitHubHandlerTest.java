package postgres.db.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import postgresql.db.*;
import postgresql.db.models.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GitHubHandlerTest {
    private GitHubHandler handler;

    @Before
    public void init() throws Exception {
        handler = new GitHubHandler();
    }
    @After
    public void finish() throws IOException {
        handler.close();
    }

    @Test
    public void getReposAndContributors() throws Exception {
        List<Repository> allRepos = new ArrayList<>(
                    Arrays.asList(handler.getMostStarredForLast8Weeks(5)));
        assertEquals(5, allRepos.size());
        allRepos.addAll(Arrays.asList(handler.getMostCommitedForLast8Weeks(7)));
        assertEquals(12, allRepos.size());

        Repository repo = allRepos.get(0);
        repo.setContributors(handler.getContributors(repo, 5));
        assertNotNull(repo.getContributors());
        assertEquals(5, repo.getContributors().length);
    }
}