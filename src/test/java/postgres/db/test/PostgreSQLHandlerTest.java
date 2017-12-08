package postgres.db.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.*;

import java.sql.*;
import java.util.List;


public class PostgreSQLHandlerTest {
    private PostgreSQLHandler handler;

    @Before
    public void init() throws Exception {
        handler = new PostgreSQLHandler("test");
    }
    @After
    public void finish() {
        handler.close();
    }

    @Test
    public void createTables() throws Exception {
        handler.createTables();
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Connection connection = DriverManager.getConnection(url + "test","postgres", "admin");
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT table_name  FROM information_schema.tables " +
                        "WHERE table_schema='public' AND table_type='BASE TABLE';");
        int effectedRowsCount = 0;
        while (result.next()) effectedRowsCount++;
        assertEquals(6, effectedRowsCount);
    }

    @Test
    public void fillTables() throws Exception {
        handler.fillTables(5, 5);
    }

    @Test
    public void selectOwnersOfProjectInCertainLanguageOrNotSpecified() throws Exception {
        List<RepositoryOwner> owners = handler.selectOwnersOfProjectInCertainLanguageOrNotSpecified("JavaScript");
        assertNotNull(owners);
    }

    @Test
    public void selectContributorsToManyRepositories() throws Exception {
        List<User> users = handler.selectContributorsToManyRepositories();
        assertNotNull(users);
    }
    @Test
    public void selectContributorsWithCertainMinimumOfCommits() throws Exception {
        List<User> users = handler.selectContributorsWithCertainMinimumOfCommits(10);
        assertNotNull(users);
    }
    @Test
    public void selectRepositoriesByDescriptionPart() throws Exception {
        List<Repository> repos = handler.selectRepositoriesByDescriptionPart("a");
        assertNotNull(repos);
    }
    @Test
    public void selectMostPopularLanguage() throws Exception {
        Language language = handler.selectMostPopularLanguage();
        assertNotNull(language);
    }
}