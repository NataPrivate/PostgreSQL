package postgres.db.test;

import org.junit.*;

import static org.junit.Assert.*;

import postgresql.db.*;
import postgresql.db.models.Language;
import postgresql.db.models.Repository;
import postgresql.db.models.RepositoryOwner;
import postgresql.db.models.User;

import java.sql.*;
import java.util.List;


public class PostgreSQLHandlerTest {
    private static PostgreSQLHandler handler = new PostgreSQLHandler("test");

    @BeforeClass
    public static void setUp() throws Exception{
        createTables();
        fillTables();
    }
    public static void createTables() throws Exception {
        handler.createTables();
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Connection connection = DriverManager.getConnection(url + "test","postgres", "admin");
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT table_name  FROM information_schema.tables " +
                "WHERE table_schema='public' AND table_type='BASE TABLE';");
        int effectedRowsCount = 0;
        while (result.next())
            effectedRowsCount++;
        connection.close();
        assertEquals(6, effectedRowsCount);
    }
    public static void fillTables() throws Exception {
        handler.fillTables(5, 5);
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        Connection connection = DriverManager.getConnection(url + "test","postgres", "postgres");
        Statement stmt = connection.createStatement();
        ResultSet repoResult = stmt.executeQuery("SELECT count(id) FROM repository;");
        if (repoResult.next())
            assertTrue(repoResult.getInt(1) >= 5);
    }
    @AfterClass
    public static void close() {
        handler.close();
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