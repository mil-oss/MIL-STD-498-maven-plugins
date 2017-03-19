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

import java.util.Date;
import java.util.List;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import java.util.Locale;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.License;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This is our maven reporting plugin
 *
 * @goal issueLog
 * @phase site
 */
public class IssueLog extends AbstractMavenReport {

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    @Override
    protected MavenProject getProject() {
        return project;
    }

    // Not used by Maven site plugin but required by API!
    @Override
    protected Renderer getSiteRenderer() {
        return null; // Nobody calls this!
    }

    // Not used by Maven site plugin but required by API!
    // (The site plugin is only calling getOutputName(), the output dir is fixed!)
    @Override
    protected String getOutputDirectory() {
        return null; // Nobody calls this!
    }

    // Abused by Maven site plugin, a '/' denotes a directory path!
    public String getOutputName() {

        //String path = "someDirectoryInTargetSite/canHaveSubdirectory/OrMore";
        String outputFilename = "issueLog";

        // The site plugin will make the directory (and regognize the '/') in the path,
        // it will also append '.html' onto the filename and there is nothing (yes, I tried)
        // you can do about that. Feast your eyes on the code that instantiates 
        // this (good luck finding it):
        // org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext
        return //path + "/" + 
                outputFilename;
    }

    public String getName(Locale locale) {
        return "Issue Log";
    }

    public String getDescription(Locale locale) {
        return "Issue Log";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

        if (project.hasParent()) {
            //TODO revisit this in the future, i think 1 VDD per project is sufficient
            //must there may be use cases for individual/module level docs
            return;
        }

        //TODO dynamic classloading/factory pattern
        IssueProvider provider = new GitHubIssueProvider();

        Sink sink = getSink();
        sink.head();
        sink.title();       //html/head/title
        sink.text(getDescription(locale));
        sink.title_();
        sink.head_();
        sink.body();

        sink.section1();    //div
        sink.sectionTitle1();
        sink.rawText(getName(locale));
        sink.sectionTitle1_();

        sink.rawText(getDescription(locale));
      
        sink.section1_();

        sink.section2();    //div
        sink.sectionTitle2();
        sink.rawText("Issues closed since last release");

        sink.sectionTitle2_();

        provider.generateChangeLogContent(sink, project);

        sink.section2_();       //end of x.y

        sink.section2();    //div
        sink.sectionTitle2();
        sink.rawText("Open Issues");

        sink.sectionTitle2_();
        provider.generateOpenIssuesContent(sink, project,false);

        sink.section2_();       //end of x.y

        sink.body_();
        sink.flush();
        sink.close();

    }

}
