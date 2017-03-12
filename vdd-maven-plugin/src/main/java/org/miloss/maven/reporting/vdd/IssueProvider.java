/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.miloss.maven.reporting.vdd;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author AO
 */
public interface IssueProvider {

    /**
     * Outputs a unordered list of issues currently open with the product that 
     * would generally be considered bugs, performance issues, usability, etc.
     * Things that a user would most likely complain about or issues with the 
     * software that could cause crashing, incomplete functionality, security 
     * issues, etc
     * 
     * Use {@link Sink#listItem()} and {@link Sink#listItem_()} per issue
     * @param sink
     * @param project 
     */
    void generateOpenIssuesContent(Sink sink, MavenProject project);

    /**
     * Outputs the change log for at least the past version and the current 
     * version as well as any issues resolved for unreleased versions (after
     * the latest release). 
     * 
     * Use {@link Sink#listItem()} and {@link Sink#listItem_()} per issue
     * @param sink
     * @param project 
     */
    void generateChangeLogContent(Sink sink, MavenProject project);
}
