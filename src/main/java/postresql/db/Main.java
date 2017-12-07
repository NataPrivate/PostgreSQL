package postresql.db;

import java.sql.SQLException;


public class Main {
    private static PostgreSQLHandler handler;

    public static void main(String[] args) {
        try {
            handler = new PostgreSQLHandler("github");
            proceedAllOperations();
            handler.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void proceedAllOperations() throws Exception {
        handler.createTables();
        handler.fillTables();
        executeSelectQueries();
    }

    private static void executeSelectQueries() throws SQLException {
        System.out.println("\n-----Owner Of Java Projects or unnamed:-----");
        for (RepositoryOwner owner : handler.selectOwnersOfProjectInCertainLanguageOrNotSpecified("Java"))
            System.out.println(owner.toString());

        System.out.println("\n-----Contributors to more than 1 repos:-----");
        for (User user : handler.selectContributorsToManyRepositories())
            System.out.println(user.toString());

        System.out.println("\n-----Contributors with more than 1300 total commits:-----");
        for (User user : handler.selectContributorsWithCertainMinimumOfCommits(1300))
            System.out.println(user.toString());

        System.out.println("\n-----Repos with 'tool' in description-----");
        for (Repository repo : handler.selectRepositoriesByDescriptionPart("tool"))
            System.out.println(repo.toString());

        System.out.println("\n-----The most popular language:-----");
        System.out.println(handler.selectMostPopularLanguage().toString());
    }
}
