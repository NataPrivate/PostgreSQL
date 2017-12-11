package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.Contributor;


public class ContributorTest {
    @Test
    public void ContributorTest() throws Exception {
        Contributor contributor = new Contributor(1,"contributor", 8);
        assertEquals(contributor.getCommitsCount(), 8);
        assertEquals(contributor.getId(), 1);
        assertEquals(contributor.getLogin(), "contributor");
        assertEquals("contributor with 8 commits", contributor.toString());
    }
}