package postgresql.db;

import postgresql.db.models.*;

import java.io.*;
import java.sql.*;
import java.util.*;


public class PostgreSQLHandler {
    private GitHubHandler gitHubHandler;
    private Connection connection;

    public PostgreSQLHandler(String DB) {
        try {
            gitHubHandler = new GitHubHandler();
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/";
            connection = DriverManager.getConnection(url + "postgres", "postgres", "postgres");

            createDatabase(DB);
            connection = DriverManager.getConnection(url + DB, "postgres", "postgres");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
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
        parameters.append("id SERIAL NOT NULL PRIMARY KEY REFERENCES simpleuser(id)");
        createTable("contributor", parameters.toString());

        parameters = new StringBuilder();
        parameters.append("id SERIAL NOT NULL PRIMARY KEY REFERENCES simpleuser(id)");
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
        parameters.append("repository_id INT NOT NULL REFERENCES repository(id), ")
                    .append("contributor_id INT NOT NULL REFERENCES contributor(id), ")
                    .append("commits_count INT NOT NULL, ")
                    .append("PRIMARY KEY (repository_id, contributor_id)");
        createTable("repository_contributor",  parameters.toString());
    }
    private void createTable(String name, String parameters) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + name + "(" + parameters + ");";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(query);
    }

    public void fillTables(int reposCount, int contributorsCount) throws Exception {
        List<Repository> allRepos = getAllRepos(reposCount, contributorsCount);
        for (Repository repo: allRepos) {
            insertLanguage(repo.getLanguage());
            insertOwner(repo.getOwner());
            insertRepository(repo);
            for (Contributor contributor : repo.getContributors()) {
                if (contributor != null) {
                    insertContributor(contributor);
                    insertLink(new Repository_Contributor(repo.getId(), contributor.getId(), contributor.getCommitsCount()));
                }
            }
        }
    }
    private List<Repository> getAllRepos(int reposCount, int contributorsCount) throws Exception {
        List<Repository> allRepos;
        File serialFile = new File("repos.datum");
        if (serialFile.exists() && serialFile.isFile())
            allRepos = deserializeRepos("repos.datum");
        else {
            allRepos = new ArrayList<>(
                    Arrays.asList(gitHubHandler.getMostStarredForLast8Weeks(reposCount)));
            allRepos.addAll(
                    Arrays.asList(gitHubHandler.getMostCommitedForLast8Weeks(reposCount)));

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
    @SuppressWarnings("unchecked")
    private List<Repository> deserializeRepos(String path) throws IOException, ClassNotFoundException {
        List<Repository> allRepos;
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream objInputStream = new ObjectInputStream(fis);
        allRepos = ((ArrayList<Repository>) objInputStream.readObject());
        objInputStream.close();
        fis.close();

        return allRepos;
    }

    private void insertLanguage(Language language) throws SQLException {
        String query = "INSERT INTO language VALUES(?) ON CONFLICT(name) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, language.getName());
        preparedStatement.executeUpdate();
    }
    private void insertRepository(Repository repo) throws SQLException {
        String query = "INSERT INTO repository(id, owner_id, name, description, language, stars_count, commits_count) " +
                            "VALUES(?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET " +
                                "name = EXECUTED.name, description = EXECUTED.description, language = EXECUTED.language " +
                                "stars_count = EXECUTED.stars_count, commits_count = EXECUTED.commits_count";
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

        String query = "INSERT INTO contributor VALUES(?) ON CONFLICT(id) DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, contributor.getId());
        preparedStatement.executeUpdate();
    }
    private void insertUser(User user) throws SQLException {
        String query = "INSERT INTO simpleuser VALUES(?, ?) ON CONFLICT(id) DO UPDATE SET login = EXECUTED.login";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, user.getId());
        preparedStatement.setString(2, user.getLogin());
        preparedStatement.executeUpdate();
    }
    private void insertLink(Repository_Contributor link) throws SQLException {
        String query = "INSERT INTO repository_contributor(repository_id, contributor_id, commits_count) VALUES(?, ?, ?) "
                        + "ON CONFLICT DO NOTHING";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setLong(1, link.getRepositoryId());
        preparedStatement.setLong(2, link.getContributorId());
        preparedStatement.setInt(3, link.getCommits_count());
        preparedStatement.executeUpdate();
    }

    public List<RepositoryOwner> selectOwnersOfProjectInCertainLanguageOrNotSpecified(String language)
                                    throws  SQLException {
        List<RepositoryOwner> owners = new ArrayList<>();
        String query = "SELECT simpleuser.* " +
                "FROM repository, simpleuser " +
                "WHERE simpleuser.id = repository.owner_id and (language = ? or language = '');";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, language);

        ResultSet result = preparedStatement.executeQuery();
        while (result.next())
            owners.add(new RepositoryOwner(result.getLong(1), result.getString(2)));

        return owners;
//        SELECT simpleuser.*
//        FROM repository, simpleuser
//        WHERE simpleuser.id = repository.owner_id and (language = 'Java' or language = '');

    }
    public List<User> selectContributorsToManyRepositories() throws SQLException {
        List<User> users = new ArrayList<>();
        Statement stmt = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT simpleuser.* ")
                .append("FROM repository_contributor, simpleuser ")
                .append("WHERE repository_contributor.contributor_id = simpleuser.id ")
                .append("GROUP BY simpleuser.id, simpleuser.login HAVING count(repository_id) > 1;");

        ResultSet result = stmt.executeQuery(query.toString());
        while (result.next())
            users.add(new User(result.getLong(1), result.getString(2)));

        return users;
//        SELECT simpleuser.*
//        FROM repository_contributor, simpleuser
//        WHERE repository_contributor.contributor_id = simpleuser.id
//        GROUP BY simpleuser.id, simpleuser.login HAVING count(repository_id) > 1;
    }
    public List<User> selectContributorsWithCertainMinimumOfCommits(int commits_count) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, login FROM ( " +
                            "SELECT DISTINCT ON(contributor_id) simpleuser.*, sum(commits_count) " +
                            "FROM repository_contributor, simpleuser " +
                            "WHERE repository_contributor.contributor_id = simpleuser.id " +
                            "GROUP BY repository_contributor.contributor_id, simpleuser.id, simpleuser.login, commits_count " +
                            "ORDER BY contributor_id DESC) c " +
                        "GROUP BY id, login, sum  HAVING sum > ? ORDER BY sum DESC;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, commits_count);

        ResultSet result = preparedStatement.executeQuery();
        while (result.next())
            users.add(new RepositoryOwner(result.getLong(1), result.getString(2)));

        return users;
//        SELECT id, login FROM (
//                SELECT DISTINCT ON(contributor_id) simpleuser.*, sum(commits_count)
//                FROM repository_contributor, simpleuser
//                WHERE repository_contributor.contributor_id = simpleuser.id
//                GROUP BY repository_contributor.contributor_id, simpleuser.id, simpleuser.login, commits_count
//                ORDER BY contributor_id DESC
//        ) c
//        GROUP BY id, login, sum  HAVING sum > 1300 ORDER BY sum DESC;
    }
    public List<Repository> selectRepositoriesByDescriptionPart(String descriptionPart) throws SQLException {
        List<Repository> repos = new ArrayList<>();
        String query = "SELECT repository.id, simpleuser.id, simpleuser.login, repository.name, " +
                "repository.description, repository.language, repository.stars_count, repository.commits_count " +
                "FROM repository, simpleuser " +
                "WHERE repository.owner_id = simpleuser.id and description LIKE ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, "%" + descriptionPart + "%");

        ResultSet result = preparedStatement.executeQuery();
        while (result.next())
            repos.add(makeUpRepo(result));

        return repos;
//        SELECT repository.id, simpleuser.id, simpleuser.login, repository.name, repository.description, repository.language, repository.stars_count, repository.commits_count
//        FROM repository, simpleuser
//        WHERE repository.owner_id = simpleuser.id and description LIKE '%tool%';
    }

    private Repository makeUpRepo(ResultSet result) throws SQLException {
        RepositoryOwner owner = new RepositoryOwner(result.getLong(2),result.getString(3));
        Language language = new Language(result.getString(6));
        int starsCount = result.getInt(7);
        int commitsCount = result.getInt(8);
        Repository repo = new Repository(result.getLong(1), owner, result.getString(4),
                result.getString(5), language, starsCount, commitsCount);

        return repo;
    }

    public Language selectMostPopularLanguage() throws  SQLException {
        Language mostPopularLanguage = null;
        Statement stmt = connection.createStatement();
        StringBuilder query = new StringBuilder("SELECT language FROM ( ")
                    .append("SELECT DISTINCT ON (language) language, count(id) ")
                    .append("FROM repository GROUP BY language ORDER BY language DESC) r ")
                .append("WHERE count = ( ")
                    .append("SELECT MAX(count) FROM ( ")
                        .append("SELECT DISTINCT ON (language) language, count(id) ")
                        .append("FROM repository GROUP BY language ORDER BY language DESC) r ")
                .append(");");

        ResultSet result = stmt.executeQuery(query.toString());
        while (result.next())
            mostPopularLanguage = new Language(result.getString(1));

        return mostPopularLanguage;
//        SELECT language FROM (
//                SELECT DISTINCT ON (language) language, count(id)
//                FROM repository GROUP BY language ORDER BY language DESC
//                ) r
//        WHERE count = (
//                SELECT MAX(count) FROM (
    //                SELECT DISTINCT ON (language) language, count(id)
    //                FROM repository GROUP BY language ORDER BY language DESC
    //                ) r
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
