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
 * @goal svd
 * @phase site
 */
public class SofwareVersionDescriptionDocumentationReportMojo extends AbstractMavenReport {

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
        String outputFilename = "svd";

        // The site plugin will make the directory (and regognize the '/') in the path,
        // it will also append '.html' onto the filename and there is nothing (yes, I tried)
        // you can do about that. Feast your eyes on the code that instantiates 
        // this (good luck finding it):
        // org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext
        return //path + "/" + 
                outputFilename;
    }

    public String getName(Locale locale) {
        return "SVD";
    }

    public String getDescription(Locale locale) {
        return "MIL-STD-498 Software Version Description Document";
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
        sink.rawText(project.getName() + "<br>");
        sink.sectionTitle1_();

        sink.rawText(getDescription(locale));
        sink.rawText("<br>");
        sink.rawText(project.getDescription());
        sink.rawText("<br>");
        sink.rawText("Version " + project.getVersion());
        sink.section1_();

        sink.section2();    //div
        sink.sectionTitle2();
        sink.rawText("1. Scope");
        sink.sectionTitle2_();
        sink.rawText("This document is the Software Version Description Document (SVD) or as it is more commonly known as, the Version Description Document (VDD) for ");
        sink.rawText(project.getName() + " version " + project.getVersion() + ". This document was produced on " + new Date().toString());
        sink.section2_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("1.1. Identification");
        sink.sectionTitle3_();

        sink.rawText("This paragraph shall contain the project ID, release precedence, "
                + "release number, and a full identification of the system and the software, "
                + "including, as applicable, identification number(s), title(s), abbreviation(s), "
                + "and version number(s).  It shall also identify the intended recipients of the VDD. ");
        //TODO this information is in the project tree
        sink.section3_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("1.2. System Overview");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall provide a brief system overview.  It "
                + "shall describe the general nature of the system, identify current "
                + "and planned operating sites, and list other relevant documents.");
        //TODO this information is not within the maven project tree, not sure 
        //what or where to get this from...
        sink.section3_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("2. Referenced Documents");
        sink.sectionTitle2_();
        sink.rawText("None");
        //TODO this information is not the project tree, we could reference other generated documents
        //for c&p in some content from an external file
        sink.section2_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("3. Version Description");
        sink.sectionTitle2_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.1  Inventory of materials released");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall list, by identifying numbers, titles, abbreviations, "
                + "dates, version numbers, and release numbers, as applicable, "
                + "all physical media (for example, listings, tapes, disks) and "
                + "associated documentation that make up the software version being released.  "
                + "It shall include applicable security and privacy considerations for these "
                + "items, safeguards for handling them, such as concerns for static "
                + "and magnetic fields, and instructions and restrictions regarding "
                + "duplication and license provisions.<br><br>");

        sink.sectionTitle4();
        sink.rawText("Artifacts");
        sink.sectionTitle4_();
        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.rawText("Group Id");
        sink.tableHeaderCell_();

        sink.tableHeaderCell();
        sink.rawText("Artifact Id");
        sink.tableHeaderCell_();

        sink.tableHeaderCell();
        sink.rawText("Version");
        sink.tableHeaderCell_();

        sink.tableHeaderCell();
        sink.rawText("License");
        sink.tableHeaderCell_();
        sink.tableRow_();

        //print out all modules in this project. note if modules are excluded due to profiles
        //they will not be listed here
        sink.tableRow();

        sink.tableCell();
        sink.rawText(project.getGroupId());
        sink.tableCell_();

        sink.tableCell();
        sink.rawText(project.getArtifactId());
        sink.tableCell_();

        sink.tableCell();
        sink.rawText(project.getVersion());
        sink.tableCell_();

        sink.tableCell();

        printLicenses(sink, project.getLicenses());

        sink.tableCell_();

        sink.tableRow_();

        for (int i = 0; i < project.getCollectedProjects().size(); i++) {

            MavenProject get = (MavenProject) project.getCollectedProjects().get(i);
            recurseModules(get, sink);
        }

        sink.table_();

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.2  Inventory of software contents");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall list, by identifying numbers, titles, "
                + "abbreviations, dates, version numbers, and release numbers, "
                + "as applicable, all computer files that make up the software "
                + "version being released.  Any applicable security and privacy"
                + " considerations shall be included.");
        //TODO some of this information is in the project tree and seems duplicative from the artifact list...
        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.3  Changes Installed");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall provide a brief overall summary of the "
                + "changes incorporated into this release.   This paragraph shall "
                + "identify, as applicable, Discrepancy Reports (DRs), System "
                + "Advisory Notices (SANs), and Heads Up Messages (HUMs) cleared "
                + "by the release, any modifications and enhancements incorporated, "
                + "and the effects, if any, of each change on the end user, "
                + "system operation and on interfaces with other hardware and "
                + "software as applicable.  This paragraph does not normally apply"
                + " to the initial release of the software for a system.<br><br>");

        provider.generateChangeLogContent(sink, project);

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.4. Adapation data");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall identify or reference all unique‑to‑site "
                + "data contained in the software version.  For software versions "
                + "after the first, this paragraph shall describe changes made "
                + "to the adaptation data.");
        //TODO some of this information is in the project tree perhaps update guide?
        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.5  Related documents");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall list by identifying numbers, titles, "
                + "abbreviations, dates, version numbers, and release numbers, "
                + "as applicable, all documents pertinent to the software version "
                + "being released but not included in the release.");
        //TODO some of this information is in the project tree
        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.6 Installation Instructions");
        sink.sectionTitle3_();
        sink.rawText("This paragraph shall list by identifying numbers, titles, "
                + "abbreviations, dates, version numbers, and release numbers, "
                + "as applicable, all documents pertinent to the software version "
                + "being released but not included in the release.");
        //TODO not sure where this comes from, but probably in the source tree
        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.7  Possible problems and known errors");
        sink.sectionTitle3_();
        sink.rawText(" This paragraph shall identify any possible problems or "
                + "known errors with the software version being released, and "
                + "instructions (either directly or by reference) for recognizing, "
                + "avoiding, correcting, or otherwise handling each one.  "
                + "The information presented shall identify and be appropriate "
                + "for those impacted.");

        provider.generateOpenIssuesContent(sink, project);

        sink.section3_();       //end of x.y

        sink.section2_();       //end of x.

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("4. Notes");
        sink.sectionTitle2_();
        sink.rawText("This section shall contain any general information that "
                + "aids in understanding this document (e.g., background information, "
                + "glossary, rationale).  This section shall include an alphabetical "
                + "listing of all acronyms, abbreviations, and their meanings as "
                + "used in this document and a list of any terms and definitions "
                + "needed to understand this document.");
        sink.section2_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("A. Appendixes");
        sink.sectionTitle2_();
        sink.rawText("Appendixes may be used to provide information published "
                + "separately for convenience in document maintenance (e.g., charts, "
                + "classified data).  As applicable, each appendix shall be referenced "
                + "in the main body of the document where the data would normally have "
                + "been provided.  Appendixes may be bound as separate documents "
                + "for ease in handling.  Appendixes shall be lettered alphabetically "
                + "(A, B, etc.).");
        sink.section2_();

        sink.body_();
        sink.flush();
        sink.close();

    }

    private void recurseModules(MavenProject project, Sink sink) {
        if (project == null) {
            return;
        }
        sink.tableRow();
        sink.tableCell();
        sink.rawText(project.getGroupId());
        sink.tableCell_();

        sink.tableCell();
        sink.rawText(project.getArtifactId());
        sink.tableCell_();

        sink.tableCell();
        sink.rawText(project.getVersion());
        sink.tableCell_();

        sink.tableCell();
        printLicenses(sink, project.getLicenses());
        sink.tableCell_();

        sink.tableRow_();

        for (int i = 0; i < project.getCollectedProjects().size(); i++) {
            MavenProject get = (MavenProject) project.getCollectedProjects().get(i);
            recurseModules(get, sink);
        }

    }

    private void printLicenses(Sink sink, List licenses) {
        for (int i = 0; i < licenses.size(); i++) {
            Object obj = licenses.get(i);
            if (obj instanceof License) {
                License l = (License) obj;
                if (l.getUrl() != null) {
                    sink.link(l.getUrl());
                }
                sink.rawText(l.getName());
                if (l.getUrl() != null) {
                    sink.link_();
                }
            }
            if ((i + 1) == licenses.size()) {

            } else {
                sink.rawText("<br>");
            }
        }

    }
}
