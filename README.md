# MIL-STD-498-maven-plugins
A collection of maven plugins to help automate the generation of MILSTD-498 software documentation artifacts

## Goals

Maven reporting plugins that can generate the following based on repository and issue tracking information

 - Software Version Document, aka Version Description Document
 - Any suggestions?
 
## How does it work?

Currently, the plugin generates much of the content from the Maven project model (pom files and whatnot). 
Change log and open issues are pulled from Github if and only if the SCM and/or Issue tracking URLs are set in the
parent pom and it points to a Github.com project.

The document is generated when runing `mvn site`.

## Usage

1. Acquire a [Github API token](https://github.com/settings/tokens)
2. Set the new token as an environmental variable Linux `export TOKEN=abc123` or Windows `set TOKEN=abc123` OR pass it to maven using `-DTOKEN=abc123`.

It hasn't been published to maven central yet, so in the mean time, you can can check out the code
`git clone https://github.com/mil-oss/MIL-STD-498-maven-plugins.git`
then
`mvn clean install`
then to test is out on this repo
`mvn site -DTOKEN=abc123`

To test it on your repo, edit your pom to include the following:

    <project>
        ...
       <reporting>
            <plugins>
                <plugin>
                    <groupId>${project.groupId}</groupId>
                    <artifactId>vdd-maven-plugin</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                    <reportSets>
                        <reportSet>
                            <reports>
                                <report>svd</report>
                            </reports>
                        </reportSet>
                    </reportSets>
                </plugin>
            </plugins>
        </reporting>
    </project>

Then give `mvn site -DTOKEN=abc123` a try.

## FAQ

### Why do I need an API token?

It's optional. Github restricts the number of queries than can be performed in a given time period. If you run into issues with too many requests, specifying the token should resolve this issue.

### Any related projects?

Yes. https://github.com/skywinder/github-changelog-generator helped me out quite a bit and understanding how they solved the change log problem was important. It (at least the general algorithm) was adopted into this library).

## Status

Under development

## Contributing
 
PR's are welcome. 


## License

Apache License 2.0
