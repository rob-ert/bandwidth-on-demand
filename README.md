# Bandwidth on Demand (BoD)

## Getting the project to work
* The whole application is configured in a properties file called `bod-default.properties`.
* The properties can be overridden in a `bod.properties` file.
* Some properties are encrypted to be able to decrypt a `BOD_ENCRYPTION_PASSWORD` environment variable should be set.

        export BOD_ENCRYPTION_PASSWORD=[password]

* To get the application working outside SURFnet you need to set the following properties

        idd.client.class = nl.surfnet.bod.idd.IddOfflineClient
        nbi.client.class = nl.surfnet.bod.nbi.NbiOfflineClient
        
* Not using SURFConext authentication makes it easier to test/run the application. To do this you can set some properties in the `bod.properties`. An example mock implementation of is our [SoS][sos-git-repo].

		hibboleth.imitate = true
		shibboleth.imitate.displayName = John Doe
		shibboleth.imitate.userId = urn:collab:person:surfguest.nl:johndoe
		shibboleth.imitate.email = johndoe@example.com


## Running the Selenium tests

The easiest way to run the selenium tests is to run the mock open social server, [SoS][sos-git-repo].
Further a (empty) `bod-selenium` database is expected.

		mvn verify -Pselenium

## Other environments
* [Jira][jira]
* [Jenkins][jenkins]
* [Nexus][nexus]
* [Sonar][sonar]

[jira]: https://atlas.dlp.surfnet.nl/jira/
[sonar]: https://atlas.dlp.surfnet.nl/sonar/
[nexus]: https://atlas.dlp.surfnet.nl/nexus/
[jenkins]: https://atlas.dlp.surfnet.nl/jenkins/
[sos-git-repo]: gitolite@atlas.dlp.surfnet.nl:sos-server

