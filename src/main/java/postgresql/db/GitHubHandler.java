package postgresql.db;

import com.google.gson.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import postgresql.db.models.Contributor;
import postgresql.db.models.Language;
import postgresql.db.models.Repository;
import postgresql.db.models.RepositoryOwner;


public class GitHubHandler {
    private String TOKEN = System.getenv("GITHUB_TOKEN");
    private int maxPageCount = 35;
    private int maxResultsCountPerPage = 30;
    private CloseableHttpClient client = HttpClients.createDefault();

    private String getJsonResult(String url) throws IOException, IllegalArgumentException {
        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", "token " + TOKEN);
        request.setHeader("Accept", "application/vnd.github.nightshade-preview+json");
        CloseableHttpResponse response = client.execute(request);
        if (response.getEntity() == null)
            return null;

        String entity = EntityUtils.toString(response.getEntity(), "UTF-8");
        return entity.contains("message") ? null : entity;
    }

    public Repository[] getMostStarredForLast8Weeks(int count) throws IOException {
        Repository[] repos = new Repository[count];
        int wantedPageCount = count / maxResultsCountPerPage + 1;
        int pageCount = wantedPageCount > maxPageCount ? maxPageCount : wantedPageCount;

        StringBuilder url;
        JsonObject jsonObject;
        JsonArray jsonRepos;
        int index;
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/search/repositories?q=created:%3E")
                    .append(getDate2MonthsEarlier())
                    .append("&sort=stars&order=desc&page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());
            if (jsonResult == null)
                continue;
            jsonObject = (JsonObject) new JsonParser().parse(jsonResult);
            jsonRepos = jsonObject.getAsJsonArray("items");
            if (jsonRepos == null || jsonRepos.size() == 0)
                break;

            index = (currentPage - 1) * maxResultsCountPerPage;
            for (int j = 0 ; j < jsonRepos.size(); j++, index++) {
                if (index >= repos.length)
                    break;

                repos[index] = convertToRepo((JsonObject) jsonRepos.get(j));
            }
        }

        return repos;
    }
    private Repository convertToRepo(JsonObject jsonRepo) throws IOException {
        long id = jsonRepo.get("id").getAsLong();
        RepositoryOwner owner = getRepositoryOwner(jsonRepo.getAsJsonObject("owner"));
        String name = jsonRepo.get("name").getAsString();
        String description = jsonRepo.get("description").isJsonNull() ? "" : jsonRepo.get("description").getAsString();
        Language language = new Language(jsonRepo.get("language").isJsonNull() ? "" : jsonRepo.get("language").getAsString());
        int starsCount = jsonRepo.get("stargazers_count").getAsInt();
        int commitsCount = getContributorsCommitsCount(owner.getLogin() + "/" + name);
        Repository repo = new Repository(id, owner, name, description, language, starsCount, commitsCount);
        return repo;
    }
    private RepositoryOwner getRepositoryOwner(JsonObject jsonOwner) {
        long id = jsonOwner.get("id").getAsLong();
        String login = jsonOwner.get("login").getAsString();
        return new RepositoryOwner(id, login);
    }

    public Repository[] getMostCommitedForLast8Weeks(int count) throws Exception {
        List<Repository> allRepos = getAllReposForLast8Weeks();
        allRepos.sort(Comparator.comparing(Repository::getCommitsCount).reversed());

        Repository[] repos = new Repository[count];
        for (int i = 0; i < repos.length && i < allRepos.size(); i++)
            repos[i] = allRepos.get(i);
        return repos;
    }
    private List<Repository> getAllReposForLast8Weeks() throws Exception {
        List<Repository> allRepos = new ArrayList<>();

        StringBuilder url;
        for (int currentPage = 1; currentPage <= maxPageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/search/repositories?q=created:%3E")
                    .append(getDate2MonthsEarlier())
                    .append("&page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());
            if (jsonResult == null)
                continue;

            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonResult);
            JsonArray jsonRepos = jsonObject.getAsJsonArray("items");
            if (jsonRepos == null || jsonRepos.size() == 0)
                break;

            jsonRepos.forEach(repo -> {
                try {
                    allRepos.add(convertToRepo((JsonObject) repo));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return allRepos;
    }
    private int getContributorsCommitsCount(String fullName) throws IOException {
        final int[] contributionsCommitsCount = {0};

        StringBuilder url;
        String jsonResult;
        JsonArray jsonContributors;
            for (int currentPage = 1; currentPage < maxPageCount; currentPage++) {
                url = new StringBuilder("https://api.github.com/repos/")
                        .append(fullName)
                        .append("/contributors")
                        .append("?page=").append(currentPage);
                jsonResult = getJsonResult(url.toString());
                if (jsonResult == null)
                    return 0;

                jsonContributors = (JsonArray) new JsonParser().parse(jsonResult);
                if (jsonContributors == null || jsonContributors.size() == 0)
                    break;

                jsonContributors.forEach(contributor ->
                        contributionsCommitsCount[0] += ((JsonObject) contributor).get("contributions").getAsInt());
            }
        return contributionsCommitsCount[0];
    }

    public Contributor[] getContributors(Repository repo, int count) throws IOException {
        if (repo == null)
            return null;

        Contributor[] contributors = new Contributor[count];
        int wantedPageCount = count / maxResultsCountPerPage + 1;
        int pageCount = wantedPageCount > maxPageCount ? maxPageCount : wantedPageCount;

        StringBuilder url;
        String jsonResult;
        JsonArray jsonContributors;
        int index;
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/repos/")
                    .append(repo.getOwner().getLogin()).append("/").append(repo.getName())
                    .append("/contributors?page=").append(currentPage);
            jsonResult = getJsonResult(url.toString());
            if (jsonResult == null)
                continue;
            jsonContributors = (JsonArray) new JsonParser().parse(jsonResult);
            if (jsonContributors == null || jsonContributors.size() == 0)
                break;

            index = (currentPage - 1) * maxResultsCountPerPage;
            for (int j = 0 ; j < jsonContributors.size(); j++, index++) {
                if (index >= contributors.length)
                    break;

                contributors[index] = convertToContributor((JsonObject) jsonContributors.get(j));
            }
        }

        return contributors;
    }
    private Contributor convertToContributor(JsonObject jsonContributor) {
        long id = jsonContributor.get("id").getAsLong();
        String login = jsonContributor.get("login").getAsString();
        int commits_count = jsonContributor.get("contributions").getAsInt();
        return new Contributor(id, login, commits_count);
    }

    private String getDate2MonthsEarlier() {
        Calendar currentDate = GregorianCalendar.getInstance();
        int positiveResultNumberOfMonths = 3;
        int previousMonthsCount = 2;
        int startNumberOfMonthInPreviousYear = 10;
        Calendar neededDate = currentDate.get(Calendar.MONTH) >= positiveResultNumberOfMonths ?
                new GregorianCalendar(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) - previousMonthsCount, currentDate.get(Calendar.DAY_OF_MONTH)) :
                new GregorianCalendar(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH) + startNumberOfMonthInPreviousYear, currentDate.get(Calendar.DAY_OF_MONTH));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(neededDate.getTime());
    }

    public void close() throws IOException {
        client.close();
    }
}
