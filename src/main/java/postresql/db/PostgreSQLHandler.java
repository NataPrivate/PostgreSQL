package postresql.db;

import java.io.*;
import java.sql.*;
import java.util.*;


public class PostgreSQLHandler {
    private GitHubHandler gitHubHandler;
    private Connection connection;

    public PostgreSQLHandler(String DB) throws ClassNotFoundException, SQLException {
        gitHubHandler = new GitHubHandler();
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/";
        connection = DriverManager.getConnection(url + "postgres","postgres", "admin");

        createDatabase(DB);
        connection = DriverManager.getConnection(url + DB,"postgres", "admin");
    }

    public void createDatabase(String name) throws SQLException {
        List<String> databases = getListOfDBs();
        if (!databases.contains(name.toLowerCase())) {
            String query = "CREATE DATABASE " + name;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
        }
    }
    private List<String> getListOfDBs() throws SQLException {
        List<String> allDBs = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery("SELECT datname FROM pg_database;");
        while (result.next())
            allDBs.add(result.getString(1));
        return allDBs;
    }

    public void createTables() throws SQLException {
        StringBuilder parameters = new StringBuilder();
        parameters.append("name VARCHAR(20) NOT NULL PRIMARY KEY");
        createTable("language", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("id SERIAL NOT NULL PRIMARY KEY, ")
                    .append("login VARCHAR(45) NOT NULL");
        createTable("simpleuser", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("id SERIAL NOT NULL PRIMARY KEY REFERENCES simple_user(id), ")
                    .append("commits_count INT NOT NULL");
        createTable("contributor", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("id SERIAL NOT NULL PRIMARY KEY REFERENCES simple_user(id)");
        createTable("repositoryowner", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("id SERIAL NOT NULL PRIMARY KEY, ")
                    .append("owner_id INT NOT NULL REFERENCES repositoryowner(id), ")
                    .append("name VARCHAR(100) NOT NULL, ")
                    .append("description TEXT NOT NULL, ")
                    .append("language VARCHAR(20) NOT NULL REFERENCES language(name), ")
                    .append( "stars_count INT, ")
                    .append("commits_count INT");
        createTable("repository", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("repository_id INT NOT NULL REFERENCES repository(id) PRIMARY KEY, ")
                    .append("contributor_id INT NOT NULL REFERENCES contributor(id) PRIMARY KEY, ")
                    .append("commits_count INT NOT NULL");
        createTable("repository_contributor",  parameters.toString());
    }
    private void createTable(String name, String parameters) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + name + "(" + parameters + ");";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(query);
    }

    public void fillTables() throws Exception {
        List<Repository> allRepos = getAllRepos();
        for (Repository repo: allRepos) {
            insertLanguage(repo.getLanguage());
            insertOwner(repo.getOwner());
            insertRepository(repo);
            for (Contributor contributor : repo.getContributors()) {
                if (contributor != null)
                    insertContributor(contributor);
            }
        }
        allRepos.forEach(repo -> fillLinkTable(repo));
    }
    private List<Repository> getAllRepos() throws Exception {
        List<Repository> allRepos;
        File serialFile = new File("repos.datum");
        if (serialFile.exists() && serialFile.isFile())
            allRepos = deserializeRepos("repos.datum");
        else {
            int reposCount = 35;
            allRepos = new ArrayList<>(
                    Arrays.asList(gitHubHandler.getMostStarredForLast4Weeks(reposCount)));
            allRepos.addAll(
                    Arrays.asList(gitHubHandler.getMostCommitedForLast4Weeks(reposCount)));

            int contributorsCount = 40;
            allRepos.forEach(repo -> {
                try {
                    repo.setContributors(gitHubHandler.getContributors(repo, contributorsCount));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            serializeRepos(allRepos, "repos.datum");
        }

        return allRepos;
    }

    private void serializeRepos(List<Repository> allRepos, String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream objOutputStream = new ObjectOutputStream(fos);
        objOutputStream.writeObject(allRepos);
        objOutputStream.close();
        fos.close();
    }
    private List<Repository> deserializeRepos(String path) throws IOException, ClassNotFoundException {
        List<Repository> allRepos;
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream objInputStream = new ObjectInputStream(fis);
        allRepos = ((ArrayList<Repository>) objInputStream.readObject());
        objInputStream.close();
        fis.close();

        return allRepos;
    }

    private void fillLinkTable(Repository repo) {
        if (repo.getContributors() == null)
            return;

        Arrays.asList(repo.getContributors()).forEach(contributor -> {
            try {
                if (contributor != null)
                    insertLink(new Repository_Contributor(repo.getId(), contributor.getId()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void insertLanguage(Language language) throws SQLException {
        String query = "INSERT INTO language VALUES(?) ON CONFLICT(name) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, language.getName());
        preparedStatement.executeUpdate();
    }
    private void insertRepository(Repository repo) throws SQLException {
        String query = "INSERT INTO repository(id, owner_id, name, description, language, stars_count, commits_count) "
                                    + "VALUES(?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, repo.getId());
        preparedStatement.setLong(2, repo.getOwner().getId());
        preparedStatement.setString(3, repo.getName());
        preparedStatement.setString(4, repo.getDescription());
        preparedStatement.setString(5, repo.getLanguage().getName());
        preparedStatement.setInt(6, repo.getStarsCount());
        preparedStatement.setInt(7, repo.getCommitsCount() );
        preparedStatement.executeUpdate();
    }
    private void insertOwner(RepositoryOwner owner) throws SQLException {
        insertUser(new User(owner.getId(), owner.getLogin()));

        String query = "INSERT INTO repositoryowner VALUES(?) ON CONFLICT(id) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, owner.getId());
        preparedStatement.executeUpdate();
    }
    private void insertContributor(Contributor contributor) throws SQLException {
        insertUser(new User(contributor.getId(), contributor.getLogin()));

        String query = "INSERT INTO contributor VALUES(?, ?) ON CONFLICT(id) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, contributor.getId());
        preparedStatement.setInt(2, contributor.getCommitsCount());
        preparedStatement.executeUpdate();
    }
    private void insertUser(User user) throws SQLException {
        String query = "INSERT INTO simpleuser VALUES(?, ?) ON CONFLICT(id) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, user.getId());
        preparedStatement.setString(2, user.getLogin());
        preparedStatement.executeUpdate();
    }
    private void insertLink(Repository_Contributor link) throws SQLException {
        String query = "INSERT INTO repository_contributor(repository_id, contributor_id) VALUES(?, ?) "
                        + "ON CONFLICT DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, link.getRepositoryId());
        preparedStatement.setLong(2, link.getContributorId());
        preparedStatement.executeUpdate();}

    public void executeQueries() {
//        SELECT simpleuser.*, contributor.commits_count
//        FROM repository_contributor, simpleuser, contributor
//        WHERE repository_contributor.contributor_id = simpleuser.id and repository_contributor.contributor_id = contributor.id
//        GROUP BY simpleuser.id, simpleuser.login,contributor.commits_count HAVING count(repository_id) > 1;

//        SELECT simpleuser.*
//        FROM repository, simpleuser where simpleuser.id = repository.owner_id and (language = 'Java' or language = '');

//        SELECT * FROM (
//                SELECT DISTINCT ON (language) language, count(id)
//                FROM repository GROUP BY language ORDER BY language DESC
//        ) r
//        ORDER BY count DESC;

//        SELECT language FROM (
//                SELECT DISTINCT ON (language) language, count(id)
//                FROM repository GROUP BY language ORDER BY language DESC
//        ) r
//        WHERE count = (
//                SELECT MAX(count) FROM (
//                SELECT DISTINCT ON (language) language, count(id)
//                FROM repository GROUP BY language ORDER BY language DESC
//        ) r
//            );

    }

    public void close() {
        try {
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
