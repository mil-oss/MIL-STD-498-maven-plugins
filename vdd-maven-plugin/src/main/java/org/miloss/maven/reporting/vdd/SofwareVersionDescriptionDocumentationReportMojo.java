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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import java.util.Locale;
import java.util.Map;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.License;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * This is our maven reporting plugin
 *
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

    /**
     * @parameter expression="${scope}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File scope;

    /**
     * @parameter expression="${identification}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File identification;
    /**
     * @parameter expression="${systemOverview}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File systemOverview;
    /**
     * @parameter expression="${referencedDocuments}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File referencedDocuments;

    /**
     * Markdown with table with other than software artifacts? perhaps
     * documentation?
     *
     * @parameter expression="${inventory}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File inventory;

    /**
     * intro for the maven artifact listing
     *
     * @parameter expression="${inventorySoftware}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File inventorySoftware;

    /**
     * intro for the issue log for items resolved since the last release
     *
     * @parameter expression="${changesInstalled}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File changesInstalled;

    /**
     * how to upgrade
     *
     * @parameter expression="${adapatationData}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File adapatationData;
    /**
     * related documentation
     *
     * @parameter expression="${relatedDocuments}"
     * @required
     * @readonly
     *
     */
    @Parameter(required = false)
    private File relatedDocuments;

    /**
     * related documentation
     *
     * @parameter expression="${installationInstructions}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File installationInstructions;

    /**
     * knownProblems
     *
     * @parameter expression="${knownProblems}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File knownProblems;

    /**
     * notes
     *
     * @parameter expression="${notes}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File notes;

    /**
     * appendix
     *
     * @parameter expression="${appendix}"
     * @required
     * @readonly
     */
    @Parameter(required = false)
    private File appendix;

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

        MarkdownParser2 md = new MarkdownParser2();

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
        sink.rawText(project.getName());
        sink.sectionTitle1_();

        sink.paragraph();
        sink.rawText(getDescription(locale));
        sink.paragraph_();

        sink.paragraph();
        sink.rawText(project.getDescription());
        sink.paragraph_();
        sink.paragraph();
        sink.rawText("Version " + project.getVersion());
        sink.paragraph_();
        sink.section1_();

        sink.section2();    //div
        sink.sectionTitle2();
        sink.rawText("1. Scope");
        sink.sectionTitle2_();

        if (scope != null && scope.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(scope);
                md.parse(reader, sink);
            } catch (FileNotFoundException ex) {
                getLog().error(ex);
            } catch (ParseException ex) {
                getLog().error(ex);
            } catch (Exception ex) {
                getLog().error(ex);
            } finally {
                try {
                    reader.close();
                } catch (Exception ex) {
                    getLog().debug(ex);
                }
            }
        } else {
            sink.rawText("This document is the Software Version Description Document (SVD) or as it is more commonly known as, the Version Description Document (VDD) for ");
            sink.rawText(project.getName() + " version " + project.getVersion() + ". This document was produced on " + new Date().toString());

        }
        sink.section2_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("1.1. Identification");
        sink.sectionTitle3_();

        renderContents(md, identification, sink, "identification");

        sink.section3_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("1.2. System Overview");
        sink.sectionTitle3_();

        renderContents(md, systemOverview, sink, "systemOverview");

        sink.section3_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("2. Referenced Documents");
        sink.sectionTitle2_();

        renderContents(md, referencedDocuments, sink, "referencedDocuments");

        sink.section2_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("3. Version Description");
        sink.sectionTitle2_();

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.1  Inventory of materials released");
        sink.sectionTitle3_();

        renderContents(md, inventory, sink, "inventory");

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.2  Inventory of software contents");
        sink.sectionTitle3_();

        renderContents(md, inventorySoftware, sink, "inventorySoftware");

        printProjectArtifacts(sink);
        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.3  Changes Installed");
        sink.sectionTitle3_();

        renderContents(md, changesInstalled, sink, "changesInstalled");

        provider.generateChangeLogContent(sink, project);

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.4. Adapation data");
        sink.sectionTitle3_();

        renderContents(md, adapatationData, sink, "adapatationData");

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.5  Related documents");
        sink.sectionTitle3_();

        renderContents(md, relatedDocuments, sink, "relatedDocuments");

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.6 Installation Instructions");
        sink.sectionTitle3_();

        renderContents(md, installationInstructions, sink, "installationInstructions");

        sink.section3_();       //end of x.y

        sink.section3();
        sink.sectionTitle3();
        sink.rawText("3.7  Possible problems and known errors");
        sink.sectionTitle3_();

        renderContents(md, knownProblems, sink, "knownProblems");

        provider.generateOpenIssuesContent(sink, project, true);

        sink.section3_();       //end of x.y

        sink.section2_();       //end of x.

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("4. Notes");
        sink.sectionTitle2_();

        renderContents(md, notes, sink, "notes");

        sink.section2_();

        sink.section2();
        sink.sectionTitle2();
        sink.rawText("Appendixes");
        sink.sectionTitle2_();

        renderContents(md, appendix, sink, "appendix");

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

    private void printProjectArtifacts(Sink sink) {
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
    }

    private void renderContents(MarkdownParser2 md, File file, Sink sink, String systemOverview0) {

        if (file != null && file.exists()) {
            FileReader reader = null;
            try {
                if (file.getName().endsWith(".vm")) {
                    //filered 
                    //project.getProperties() these are things that could be filtered
                    //buffer the input file
                    //find and replace for all properties plus the usually markers
                    String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
                    Iterator<Map.Entry<Object, Object>> iterator = project.getProperties().entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Object, Object> next = iterator.next();
                        content = content.replace("${" + next.getKey().toString() + "}", next.getValue().toString());
                    }
                    content = content.replace("${project.version}", project.getVersion());
                    content = content.replace("${project.parent.version}", project.getVersion());
                    content = content.replace("${project.groupId}", project.getGroupId());
                    content = content.replace("${project.parent.groupId}", project.getGroupId());
                    
                    md.parse(content, sink);
                } else {
                    reader = new FileReader(file);
                    md.parse(reader, sink);
                }
            } catch (FileNotFoundException ex) {
                getLog().error(ex);
            } catch (ParseException ex) {
                getLog().error(ex);
            } catch (Exception ex) {
                getLog().error(ex);
            } finally {
                try {
                    reader.close();
                } catch (Exception ex) {
                    getLog().debug(ex);
                }
            }
        } else {
            sink.rawText("No content was defined via the '" + systemOverview0 + "' configuration parameter");
        }
    }

}
