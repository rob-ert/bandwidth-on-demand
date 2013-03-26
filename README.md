# Bandwidth on Demand (BoD)

* The Bandwidth on Demand (BoD) projects main purpose is to reservere bandwith for a specific amount of time. Like booking a hotelroom.
* BoD serves three groups of users, being the Network Operation Control Engineers (NOC), Administrators of an institute (a customer of SURFnet) and the enduser (e.g. scientist) who actually needs the bandwith to be available between certain locations.
* BoD provides a web user interface as well as a webservice interface which applies to the NSI (network services interface) specification.
* BoD connects to the SURFnet Customer Relation Management system called IDD to retrieve customer information and to a Network Management System (NMS) to retrieve network information and to delegate the reservation requests. Currently OpenDrac is the underlying NMS, but in 2013 the newly build OneControl NMS of Cienna will be used.

## Getting the project to work
* The application is configured in a properties file called `bod-default.properties`. The properites file is configured so that the application should run out of the box. All the external dependencies are replaced by offline/mock clients. The different bod environments are configured in a seperate git repo that is a git submodule of this project. Only team members will have access to this repo.
* By default the application will not use SURFconext authentication nor SAB (another SURFnet service) but a mock implementation. The sos-server should be running to use bod locally. The server is found here [SoS][sos] and can be started with `mvn jetty:run`.
* You need to set the environment variable `BOD_ENCRYPTION_PASSWORD` to some value. It will only be used for encrypted properties which are not present in the `bod-default.properties` file.
* The project depends on the `bod-mtosi` jar. This jar is not publically available. The default configuration will not need MTOSI, remove the dependeny from the `pom.xml` to get the project building.

## Running the Selenium tests
The easiest way to run the selenium tests is to run the mock open social server, [SoS][sos].
Further a (empty) `bod-selenium` database is expected.

    mvn verify -Pselenium

If you want to run the selenium tests from your IDE (like Eclipse), you could start a local server with selenium settings.

    mvn jetty:run -Pselenium-server

## Running the integration tests
For the integration tests access to the `bod-env-properties` git repo is needed. You can run them with

    mvn verify -Pintegration

## Running the Gatling tests
Load testing is done with [Gatling][gatling]. The simulations are located in `src/test/gatling/simulations`.
To run a simulation like `NsiReserveRequestSimulation` type:

    mvn gatling:execute -Dgatling.simulationClass=NsiReserveRequestSimulation

### Java 7
BoD requires Java 7. Mainly because of jax-ws 2.2 which bundled in java 7, java 6 uses jax-ws 2.1. If you are on a Mac like me, you need to download the dmg from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html). After installation you have a `/Library/Java/JavaVirtualMachines/1.7.0.jdk` directory.

To make sure maven is using your jdk 7 set your env JAVA_HOME to ``/usr/libexec/java_home --version 1.7``. Check with `mvn -version`.

To change the default jre in mac os X open java preferences.


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

## OAuth2 and NSI requests

For making NSI requests an OAuth2 access token is needed. If you want to generate or validate these tokens in BoD an OAuth2 server is needed.  
We use a the [apis][apis] server for this.

[jasypt]: http://www.jasypt.org/
[opendrac]: https://www.opendrac.org/
[opendrac-app]: http://drac.surfnet.nl:8443/
[sos]: https://github.com/BandwidthOnDemand/sos-server
[gatling]: http://gatling-tool.org
[apis]: https://github.com/OpenConextApps/apis
