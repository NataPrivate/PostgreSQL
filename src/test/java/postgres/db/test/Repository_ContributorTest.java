package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.Repository_Contributor;


public class Repository_ContributorTest {
    @Test
    public void Repository_ContributorTest() throws Exception {
        Repository_Contributor link = new Repository_Contributor(1, 1, 3);
        assertEquals(link.getCommits_count(), 3);
        assertEquals(link.getContributorId(), 1);
        assertEquals(link.getRepositoryId(), 1);
    }
}