package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.User;


public class UserTest {
    @Test
    public void UserTest() throws Exception {
        User user = new User(1,"user");
        assertEquals("user with id: 1", user.toString());

        user = new User();
        assertNull(user.getLogin());
        assertEquals(user.getId(), 0);
        assertEquals("Unknown with id: 0", user.toString());
    }
}