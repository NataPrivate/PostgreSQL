package postresql.db;

import com.google.gson.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;


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
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public Repository[] getMostStarredForLast4Weeks(int count) throws IOException {
        Repository[] repos = new Repository[count];
        int wantedPageCount = count / maxResultsCountPerPage + 1;
        int pageCount = wantedPageCount > maxPageCount ? maxPageCount : wantedPageCount;

        StringBuilder url;
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/search/repositories?q=created:%3E")
                    .append(getDate2MonthsEarlier())
                    .append("&sort=stars&order=desc&page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonResult);
            JsonArray jsonRepos = jsonObject.getAsJsonArray("items");
            if (jsonRepos == null || jsonRepos.size() == 0)
                break;

            int index = (currentPage - 1) * maxResultsCountPerPage;
            for (int j = 0 ; j < jsonRepos.size(); j++, index++) {
                if (index >= repos.length)
                    break;

                JsonObject currentRepoJson = (JsonObject) jsonRepos.get(j);
                repos[index] = convertToRepo(currentRepoJson);
                repos[index].setStarsCount(getStarsCount(currentRepoJson));
            }
        }

        return repos;
    }
    private Repository convertToRepo(JsonObject jsonRepo) {
        long id = jsonRepo.get("id").getAsLong();
        RepositoryOwner owner = getRepositoryOwner(jsonRepo.getAsJsonObject("owner"));
        String name = jsonRepo.get("name").getAsString();
        String description = jsonRepo.get("description").isJsonNull() ? "" : jsonRepo.get("description").getAsString();
        Language language = new Language(jsonRepo.get("language").isJsonNull() ? "" : jsonRepo.get("language").getAsString());
        return new Repository(id, owner, name, description, language);
    }
    private RepositoryOwner getRepositoryOwner(JsonObject jsonOwner) {
        long id = jsonOwner.get("id").getAsLong();
        String login = jsonOwner.get("login").getAsString();
        return new RepositoryOwner(id, login);
    }
    private int getStarsCount(JsonObject jsonRepo) {
        return jsonRepo.get("stargazers_count").getAsInt();
    }

    public Repository[] getMostCommitedForLast4Weeks (int count) throws Exception {
        List<Repository> allRepos = getAllReposForLast4Weeks();
        allRepos.sort(Comparator.comparing(Repository::getCommitsCount).reversed());

        Repository[] repos = new Repository[count];
        for (int i = 0; i < repos.length; i++)
            repos[i] = allRepos.get(i);
        return repos;
    }
    private List<Repository> getAllReposForLast4Weeks() throws Exception {
        List<Repository> allRepos = new ArrayList<>();

        StringBuilder url;
        for (int currentPage = 1; currentPage <= maxPageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/search/repositories?q=created:%3E")
                    .append(getDate2MonthsEarlier())
                    .append("&page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());
            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonResult);
            JsonArray jsonRepos = jsonObject.getAsJsonArray("items");
            if (jsonRepos == null || jsonRepos.size() == 0)
                break;

            jsonRepos.forEach(repo -> allRepos.add(convertToRepo((JsonObject)repo)));
        }
        allRepos.forEach(repo -> {
            try {
                repo.setCommitsCount(getContributorsCommitsCount(repo));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        return allRepos;
    }
    private int getContributorsCommitsCount(Repository repo) throws IOException {
        final int[] contributionsCommitsCount = {0};

        StringBuilder url;
        for (int currentPage = 1; currentPage < maxPageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/repos/")
                    .append(repo.getOwner().getLogin()).append("/").append(repo.getName())
                    .append("/contributors")
                    .append("?page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());

            JsonArray jsonContributors = (JsonArray) new JsonParser().parse(jsonResult);
            if (jsonContributors == null || jsonContributors.size() == 0)
                break;

            jsonContributors.forEach(contributor ->
                    contributionsCommitsCount[0] += ((JsonObject)contributor).get("contributions").getAsInt());
        }

        return contributionsCommitsCount[0];
    }

    public Contributor[] getContributors(Repository repo, int count) throws IOException {
        Contributor[] contributors = new Contributor[count];
        int wantedPageCount = count / maxResultsCountPerPage + 1;
        int pageCount = wantedPageCount > maxPageCount ? maxPageCount : wantedPageCount;

        StringBuilder url;
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            url = new StringBuilder("https://api.github.com/repos/")
                    .append(repo.getOwner().getLogin()).append("/").append(repo.getName())
                    .append("/contributors?page=").append(currentPage);
            String jsonResult = getJsonResult(url.toString());
            JsonArray jsonContributors = (JsonArray) new JsonParser().parse(jsonResult);
            if (jsonContributors == null || jsonContributors.size() == 0)
                break;

            int index = (currentPage - 1) * maxResultsCountPerPage;
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
