/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.miloss.maven.reporting.vdd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN;
import org.miloss.github.Release;

/**
 * Issue provider for github
 * @author AO
 */
public class GitHubIssueProvider implements IssueProvider{

    @Override
    public void generateOpenIssuesContent(Sink sink, MavenProject project) {
        if ((project.getScm() != null && project.getScm().getDeveloperConnection().contains("https://github.com/"))
                || (project.getIssueManagement() != null && project.getIssueManagement().getUrl().contains("https://github.com/"))) {

            try {
                //OAuth2 token authentication
                GitHubClient client = new GitHubClient();
                String token = System.getenv("TOKEN");
                if (System.getProperty("TOKEN") != null) {
                    token = System.getProperty("TOKEN");
                }

                client.setOAuth2Token(token);

                IssueService issues = new IssueService(client);

                RepositoryId toBeForked = getRepo(project);
                if (toBeForked == null) {
                    throw new Exception("Unable to identify the Github repository, try setting the scm developer connection or the issue management url in the pom");
                }
                Map<String, String> filters = new HashMap<>();
                filters.put(IssueService.FILTER_STATE, STATE_OPEN);
                List<Issue> issues1;

                issues1 = issues.getIssues(toBeForked, filters);
                sink.list();
                for (int i = 0; i < issues1.size(); i++) {
                    Issue get = issues1.get(i);
                    if (hasBugLabel(get.getLabels())) {
                        sink.listItem();
                           sink.link(get.getHtmlUrl());
                        sink.rawText("[#" + get.getNumber() + "] ");
                        sink.link_();
                        
                        sink.rawText(get.getTitle() + " ");
                     
                        sink.listItem_();
                    }

                }
                sink.list_();
            } catch (Exception ex) {
                sink.rawText("<br><b>Unable to retrieve the issue list from Github: " + ex.getMessage() + "</b><br>");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void generateChangeLogContent(Sink sink, MavenProject project) {
        if ((project.getScm() != null && project.getScm().getDeveloperConnection().contains("https://github.com/"))
                || (project.getIssueManagement() != null && project.getIssueManagement().getUrl().contains("https://github.com/"))) {

            try {

                //OAuth2 token authentication
                GitHubClient client = new GitHubClient();
                String token = System.getenv("TOKEN");
                if (System.getProperty("TOKEN") != null) {
                    token = System.getProperty("TOKEN");
                }

                client.setOAuth2Token(token);

                RepositoryId toBeForked = RepositoryId.createFromUrl(getRepoUrl(project));
                String githubUrl = getRepoUrl(project);
                githubUrl = githubUrl.substring(0, githubUrl.length() - 6);

                if (toBeForked == null) {
                    throw new Exception("Unable to identify the Github repository, try setting the scm developer connection or the issue management url in the pom");
                }

                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

                String url = "https://api.github.com/repos/" + toBeForked.generateId() + "/releases";

                URL git = new URL(url);
                InputStream openStream = git.openStream();
                List<Release> releases = mapper.readValue(openStream, new TypeReference<ArrayList<Release>>() {
                });
                openStream.close();

                //sort the releases by release date.
                Collections.sort(releases, new Comparator<Release>() {
                    @Override
                    public int compare(Release o1, Release o2) {
                        try {
                            return o2.getReleaseDate().compareTo(o1.getReleaseDate());
                        } catch (ParseException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                        return 0;
                    }
                });
               // System.out.println(myObjects.size() + " releases discovered");

                //get the change log from now up until the last release (which is probably now)
                //if the issue list is empty, skip
                //[Unreleased](https://github.com/mil-oss/fgsms/tree/HEAD)
                

                if (toBeForked == null) {
                    throw new Exception("Unable to identify the Github repository, try setting the scm developer connection or the issue management url in the pom");
                }

                List<Issue> issues = getIssues(client, toBeForked);    //all issues

                Date from = new Date();
                Date until = null;

                if (releases.isEmpty()) {

                    //never been a release
                    //go from now until the begining of time
                    until = new Date(0);
                    processIssues(issues, from, until, sink);
                } else {
                    Release get = releases.get(0);
                    //from now since the last release
                    until = get.getReleaseDate();
                    sink.bold();
                    sink.rawText("Issures resolve since the last release " + get.getName() + " " + get.getReleaseDate().toString());
                    sink.bold_();
                    sink.rawText("<br>");
                    sink.list();
                    processIssues(issues, from, until, sink);
                    sink.list_();

                    //            
                    for (int i = 0; i < releases.size() - 1; i++) {
                        get = releases.get(i);
                        from = get.getReleaseDate();
                        until = releases.get(i + 1).getReleaseDate();
                        sink.bold();
                        sink.rawText("Release " + get.getName() + " " + get.getReleaseDate().toString());
                        sink.bold_();
                        sink.rawText("<br>");
                        
                        sink.list();
                        processIssues(issues, from, until, sink);
                        sink.list_();

                    }
                }

            } catch (Exception ex) {
                sink.rawText("<br><b>Unable to retrieve the issue list from Github: " + ex.getMessage() + "</b><br>");
                ex.printStackTrace();
            }
        }
    }
 
    
    private static String getRepoUrl(MavenProject project) {

        if (project != null && project.getIssueManagement() != null && project.getIssueManagement().getUrl() != null) {
            String url = project.getIssueManagement().getUrl();
            //https://github.com/mil-oss/fgsms/issues
            if (url.startsWith("https://github.com/")) {
                return url;
            }

        }

        if (project != null && project.getScm() != null && project.getScm().getDeveloperConnection() != null) {
            String url = project.getScm().getDeveloperConnection();
            if (url.contains("https://github.com/")) {
                //scm:git:https://github.com/mil-oss/fgsms.git
                if (url.startsWith("scm:git")) {
                    url = url.replace("scm:git", "");
                }
                if (url.endsWith(".git")) {
                    url = url.substring(0, url.length() - 5);
                }
                return url;
            }
        }

        return null;
    }

    private List<Issue> getIssues(GitHubClient client, RepositoryId toBeForked) {
        //need to get the date of the previous tag
        //if it exists, then find all issues resolved since then
        try {

            IssueService issues = new IssueService(client);

            Map<String, String> filters = new HashMap<>();
            filters.put(IssueService.FILTER_STATE, IssueService.STATE_CLOSED);
            List<Issue> issues1;

            issues1 = issues.getIssues(toBeForked, filters);

            Collections.sort(issues1, new Comparator<Issue>() {
                @Override
                public int compare(Issue o1, Issue o2) {

                    return o2.getClosedAt().compareTo(o1.getClosedAt());

                }
            });
            return issues1;

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return null;
    }

    private int processIssues(List<Issue> issues, Date from, Date until, Sink sink) {
        int count = 0;
        for (int i = 0; i < issues.size(); i++) {
            Issue get = issues.get(i);
            if (get.getClosedAt().before(from) && get.getClosedAt().after(until)) {
                sink.listItem();
                sink.link(get.getHtmlUrl());
                sink.rawText("[#" + get.getNumber() + "] ");
                sink.link_();
                sink.rawText(" " + get.getTitle());
                sink.listItem_();
                count++;
            }
        }
        if (count == 0) {
            sink.listItem();
            sink.rawText("No issues logged.");
            sink.listItem_();
        }
        return count;
    }
    
    private RepositoryId getRepo(MavenProject project) {

        if (project != null && project.getIssueManagement() != null && project.getIssueManagement().getUrl() != null) {
            String url = project.getIssueManagement().getUrl();
            //https://github.com/mil-oss/fgsms/issues
            if (url.startsWith("https://github.com/")) {
                url = url.replace("https://github.com/", "");
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.endsWith("issues")) {
                url = url.substring(0, url.length() - 7);
            }

            String[] parts = url.split("/");
            if (parts.length == 2) {
                String org = parts[0];
                String repo = parts[1];
                return new RepositoryId(org, repo);
            }
        }

        if (project != null && project.getScm() != null && project.getScm().getDeveloperConnection() != null) {
            String url = project.getScm().getDeveloperConnection();
            //scm:git:https://github.com/mil-oss/fgsms.git
            if (url.startsWith("scm:git")) {
                url = url.replace("scm:git", "");
            }
            url = url.replace("https://github.com/", "");

            String[] parts = url.split("/");
            if (parts.length == 2) {
                String org = parts[0];

                String repo = parts[1];
                if (repo.endsWith(".git")) {
                    repo = repo.substring(0, repo.length() - 4);
                }
                return new RepositoryId(org, repo);
            }
        }

        return null;
    }

    private boolean hasBugLabel(List<Label> labels) {
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).getName().equalsIgnoreCase("bug")
                    || labels.get(i).getName().equalsIgnoreCase("defect")
                    || labels.get(i).getName().equalsIgnoreCase("security")) {
                return true;
            }
        }
        return false;
    }
}
