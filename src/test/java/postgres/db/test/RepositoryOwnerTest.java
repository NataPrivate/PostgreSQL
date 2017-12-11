package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.RepositoryOwner;


public class RepositoryOwnerTest {
    @Test
    public void RepositoryOwnerTests() throws Exception {
        RepositoryOwner owner = new RepositoryOwner(1,"owner");
        assertEquals(owner.getId(), 1);
        assertEquals(owner.getLogin(), "owner");
        assertEquals("owner with id: 1", owner.toString());
    }
}