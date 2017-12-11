package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.Language;
import postgresql.db.models.Repository;
import postgresql.db.models.RepositoryOwner;


public class RepositoryTest {
    @Test
    public void RepositoryTest() throws Exception {
        RepositoryOwner owner = new RepositoryOwner(1, "owner");
        Language language = new Language("Java");
        Repository repo = new Repository(1, owner, "repo", "testRepo", language, 0, 11);
        assertEquals(repo.getId(), 1);
        assertEquals(repo.getOwner(), owner);
        assertEquals(repo.getName(), "repo");
        assertEquals(repo.getDescription(), "testRepo");
        assertEquals(repo.getLanguage(), language);
        assertEquals(repo.getStarsCount(), 0);
        assertEquals(repo.getCommitsCount(), 11);
        assertEquals(repo.toString(), "\nowner: owner\n" +
                                            "name: repo\n" +
                                            "description: testRepo\n" +
                                            "language: Java\n" +
                                            "starsCount: 0\n" +
                                            "commitsCount: 11");
    }
}