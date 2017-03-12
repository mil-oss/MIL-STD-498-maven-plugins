/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.miloss.test;

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
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.miloss.github.Release;

/**
 * A test app to test generation of VDD without the maven complexities and to 
 * force certain pom conditions as test parameters
 * @author AO
 */
public class Main {

    public static void main(String[] args) throws Exception {

        try {
            //https://api.github.com/repos/mil-oss/fgsms/releases
            //OAuth2 token authentication
            GitHubClient client = new GitHubClient();
            String token = System.getenv("TOKEN");
            if (System.getProperty("TOKEN") != null) {
                token = System.getProperty("TOKEN");
            }

            

            client.setOAuth2Token(token);

            RepositoryId toBeForked = RepositoryId.createFromUrl("https://github.com/mil-oss/fgsms");
            String githubUrl = "https://github.com/mil-oss/fgsms/issues/";
            if (githubUrl.endsWith("/")) {
                githubUrl = githubUrl.substring(0, githubUrl.length() - 8);
            } else {
                githubUrl = githubUrl.substring(0, githubUrl.length() - 7);
            }

            if (toBeForked == null) {
                throw new Exception("Unable to identify the Github repository, try setting the scm developer connection or the issue management url in the pom");
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

            String url = "https://api.github.com/repos/" + toBeForked.generateId() + "/releases";

            URL git = new URL(url);
            InputStream openStream = git.openStream();
            List<Release> myObjects = mapper.readValue(openStream, new TypeReference<ArrayList<Release>>() {
            });
            openStream.close();

            //sort the releases by release date.
            Collections.sort(myObjects, new Comparator<Release>() {
                @Override
                public int compare(Release o1, Release o2) {
                    //want newest to oldest
                    try {
                        return o2.getReleaseDate().compareTo(o1.getReleaseDate());
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    return 0;
                }
            });
            System.out.println(myObjects.size() + " releases discovered");

            List<Issue> issues = getIssues(client, toBeForked);

            Date from = new Date();
            Date until = null;

            if (myObjects.isEmpty()) {

                //never been a release
                //go from now until the begining of time
                until = new Date(0);
                processIssues(issues, from, until);
            } else {
                Release get = myObjects.get(0);
                //from now since the last release
                until = get.getReleaseDate();
                System.out.println("Unreleased changes after " + get.getName() + " " + get.getReleaseDate().toString());
                            processIssues(issues, from, until);

                //            
                for (int i = 0; i < myObjects.size()-1; i++) {
                    get = myObjects.get(i);
                    from = get.getReleaseDate();
                    until = myObjects.get(i+1).getReleaseDate();

                    System.out.println("Release " + get.getName() + " " + get.getReleaseDate().toString());
                    processIssues(issues, from, until);

                }           
            }
            

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private static List<Issue> getIssues(GitHubClient client, RepositoryId toBeForked) {
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

    private static int processIssues(List<Issue> issues, Date from, Date until) {
        int count = 0;
        for (int i = 0; i < issues.size(); i++) {
            Issue get = issues.get(i);
            if (get.getClosedAt().before(from) && get.getClosedAt().after(until)) {
                System.out.println(get.getClosedAt() + " " + get.getNumber() + " " + get.getTitle());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("No issues logged");
        }
        return count;
    }
}
