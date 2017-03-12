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
