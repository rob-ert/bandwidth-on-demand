# Bandwidth on Demand (BoD)
* The Bandwidth on Demand (BoD) projects main purpose is to reservere bandwith for a specific amount of time. Like booking a hotelroom. 
* BoD serves three groups of user, being the Network Operation Control Engineers (NOC), Managers of an institute (a customer of SURFnet) and the enduser (e.g. scientist) who actually needs the bandwith to be available between certain locations.
* BoD provides a web user interface as well as a webservice interface which applies to the NSI (network services interface) specification.
* BoD connects to the SURFnet Customer Relation Management system called IDD to retrieve customer information and to a Network Management System (NMS) to retrieve network information and to delegate the reservation requests. Currently OpenDrac is the underlying NMS, but in 2013 the newly build OneControl NMS of Cienna will be used.
* This project consists out of the following projects:

## Getting the project to work
* The whole application is configured in a properties file called `bod-default.properties`.
* The properties can be overridden in a `bod.properties` file that should be on the classpath.
* Some properties are encrypted to be able to decrypt a `BOD_ENCRYPTION_PASSWORD` environment variable should be set.

        export BOD_ENCRYPTION_PASSWORD=[password]

* To get the application working outside SURFnet you need to set the following properties

        idd.client.class = nl.surfnet.bod.idd.IddOfflineClient
        nbi.client.class = nl.surfnet.bod.nbi.NbiOfflineClient

* Not using SURFconext authentication makes it easier to test/run the application. To do this you can set some properties in the `bod.properties`. An example mock implementation of is our [SoS][sos-git-repo].

    hibboleth.imitate = true
    shibboleth.imitate.displayName = John Doe
    shibboleth.imitate.userId = urn:collab:person:surfguest.nl:johndoe
    shibboleth.imitate.email = johndoe@example.com

### Java 7
BoD requires Java 7. Mainly because of jax-ws 2.2 which bundled in java 7, java 6 uses jax-ws 2.1. If you are on a Mac like me, you need to download the dmg from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html). After installation you have a `/Library/Java/JavaVirtualMachines/1.7.0.jdk` directory.

If you are using Eclipse you have to add the jdk. Eclipse > Properties > Java > Installed JREs. Add a JRE with JRE home set to `/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home`.

To make sure maven is using your jdk 7 set your env JAVA_HOME to ``/usr/libexec/java_home --version 1.7``. Check with `mvn -version`.

To change the default jre in mac os X open java preferences.

To decypt some properties you need to replace your `policy.jar` and `US_export_policy.jar`. The unlimited strength policy files are also on the [Oracle download site](http://www.oracle.com/technetwork/java/javase/downloads/index.html) at the bottom. Go to `/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/jre/lib/security` and replace the tow jar files with the ones in the jce zip file.

### Postgresql
When running postgresql on a Mac installed through Homebrew your conf file says it can have a maximum of 20 connections. When you would like to increase this because you are running out of connections you could do the following. Edit the `/usr/local/var/postgres/postgresl.conf` and change the `max_connections` to 100 for example. 
To make postgresql happy you should change some memmory settings by editing `/etc/sysctl.conf`

    kern.sysv.shmmax=1073741824
    kern.sysv.shmmin=1
    kern.sysv.shmmni=4096
    kern.sysv.shmseg=32
    kern.sysv.shmall=1179648

And restarting postgresql (for example) like

    launchctl unload ~/Library/LaunchAgents/org.postgresql.postgres.plist
    launchctl load ~/Library/LaunchAgents/org.postgresql.postgres.plist


## Running the Selenium tests

The easiest way to run the selenium tests is to run the mock open social server, [SoS][sos-git-repo].
Further a (empty) `bod-selenium` database is expected.

    mvn verify -Pselenium

If you want to run the selenium tests from your IDE (like Eclipse), you could start a local server with selenium settings.

    mvn jetty:run -Pselenium-server

## Running the Gatling tests

Load testing is done with [Gatling][gatling]. The simulations are located in `src/test/gatling/simulations`.
To run them type:

    mvn gatling:execute -Dgatling.simulation=XXSimulation

## Network Managment System (NMS)

At the moment [openDRAC][opendrac] is used as our NMS. The webservice needs a username and password. The password is in the bod-default.properties file and is encrypted with [Jasypt][jasypt]. The webservice uses a different password as the [web application][opendrac-app]. But should be able to login to verify ports or reservations.  
To easily test the encryption and decryption there exists a helper class on the test classpath named `EncryptionHelper`.

## Other environments
* [Jira][jira]
* [Jenkins][jenkins]
* [Nexus][nexus]
* [Sonar][sonar]

## Setting up Shibboleth

How to setup Shibboleth can be found on the [SURFconext wiki](https://wiki.surfnetlabs.nl/display/surfconextdev/My+First+SP+-+Shibboleth).

[jasypt]: http://www.jasypt.org/
[opendrac]: https://www.opendrac.org/
[opendrac-app]: http://drac.surfnet.nl:8443/
[jira]: https://atlas.dlp.surfnet.nl/jira/
[sonar]: https://atlas.dlp.surfnet.nl/sonar/
[nexus]: https://atlas.dlp.surfnet.nl/nexus/
[jenkins]: https://atlas.dlp.surfnet.nl/jenkins/
[sos-git-repo]: gitolite@atlas.dlp.surfnet.nl:sos-server
[gatling]: http://gatling-tool.org
