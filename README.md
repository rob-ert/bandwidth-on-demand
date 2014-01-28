# Bandwidth on Demand (BoD) #

* The Bandwidth on Demand (BoD) projects main purpose is to reserve
  bandwidth from point A to B for a specific duration. Like booking a
  hotelroom.
* BoD serves three groups of users, being the Network Operation
  Control Engineers (NOC), Administrators of an institute (a customer
  of SURFnet) and the enduser (e.g. scientist) who actually needs the
  bandwidth to be available between certain locations.
* BoD provides a web user interface as well as a webservice interface
  which applies to the NSI (network services interface) specification.
* BoD connects to the SURFnet Customer Relation Management system
  called IDD to retrieve customer information and to a Network
  Management System (NMS) to retrieve network information and to
  delegate the reservation requests. Currently OpenDRAC is the
  underlying NMS, but in 2013 the newly build OneControl NMS of Ciena
  will be used.

## Getting the project to work ##

* Create databases

        createuser bod_user -R -S -D
        createdb -E 'UTF-8' -O bod_user bod

* The application is configured in a properties file called
  `bod-default.properties`. The properites file is configured so that
  the application should run out of the box. All the external
  dependencies are replaced by offline/mock clients. The different bod
  environments are configured in a seperate git repo that is a git
  submodule of this project. Only team members will have access to
  this repo.
* By default the application will not use SURFconext authentication
  nor SAB (another SURFnet service) but a mock implementation. The
  sos-server should be running to use bod locally. The server is found
  here [SoS][sos] and can be started with `mvn jetty:run`.
* You need to set the environment variable `BOD_ENCRYPTION_PASSWORD`
  to some value. It will only be used for encrypted properties which
  are not present in the `bod-default.properties` file.
* The project depends on the `bod-mtosi` jar. This jar is not publicly
  available. The default configuration will not need MTOSI, remove the
  dependency from the `pom.xml` to get the project building.

## Running the Selenium tests ##

An (empty) `bod-selenium` database is expected.

    createuser bod-selenium_user -R -S -D
    createdb -E 'UTF-8' -O bod-selenium_user bod-selenium

Running all the selenium tests

    mvn verify -Pselenium

Running a single test

    mvn verify -Pselenium -Dit.test=ReservationTestSelenium

If you want to run the selenium tests from your IDE (like Eclipse),
you could start a local server with selenium settings.

    mvn jetty:run -Pselenium-server

## Running the integration tests ##

For the integration tests access to the `bod-env-properties` git repo
is needed and an empty db.

    createuser bod-integration_user -R -S -D
    createdb -E 'UTF-8' -O bod-integration_user bod-integration

You can run them with

    mvn verify -Pintegration

## Running the Gatling tests ##

Load testing is done with [Gatling][gatling]. The simulations are
located in `src/test/gatling/simulations`.  To run a simulation like
`NsiReserveRequestSimulation` type:

    mvn gatling:execute -Dgatling.simulationClass=NsiReserveRequestSimulation

### Java 7 ###

BoD requires Java 7. Mainly because of jax-ws 2.2 which bundled in
java 7, java 6 uses jax-ws 2.1. If you are on a Mac like me, you need
to download the dmg from
[Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html). After
installation you have a `/Library/Java/JavaVirtualMachines/1.7.0.jdk`
directory.

To make sure maven is using your jdk 7 set your env JAVA_HOME to
``/usr/libexec/java_home --version 1.7``. Check with `mvn -version`.

To change the default jre in mac os X open java preferences.

You also need to install the unlimited encryption extensions. These
can be downloaded from
[Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html).

### Postgresql

When running postgresql on a Mac installed through Homebrew your conf
file says it can have a maximum of 20 connections. When you would like
to increase this because you are running out of connections you could
do the following. Edit the `/usr/local/var/postgres/postgresl.conf`
and change the `max_connections` to 100 for example.  To make
postgresql happy you should change some memmory settings by editing
`/etc/sysctl.conf`

    kern.sysv.shmmax=1073741824
    kern.sysv.shmmin=1
    kern.sysv.shmmni=4096
    kern.sysv.shmseg=32
    kern.sysv.shmall=1179648

And restarting postgresql (for example) like

    launchctl unload ~/Library/LaunchAgents/org.postgresql.postgres.plist
    launchctl load ~/Library/LaunchAgents/org.postgresql.postgres.plist

## OAuth2 and NSI requests ##

For making NSI requests an OAuth2 access token is needed. If you want
to generate or validate these tokens in BoD an OAuth2 server is
needed. We use a the [apis][apis] server for this.

[jasypt]: http://www.jasypt.org/
[opendrac]: https://www.opendrac.org/
[opendrac-app]: http://drac.surfnet.nl:8443/
[sos]: https://github.com/BandwidthOnDemand/sos-server
[gatling]: http://gatling-tool.org
[apis]: https://github.com/OpenConextApps/apis


## Using Onecontrol
By default, the application runs in 'opendrac-offline' mode. If you need to talk to a onecontrol server, you will need to install haproxy and configure it to run in Onecontrol
mode
This is easy to do. First, install haproxy:
    brew install haproxy
Then, create a file called `haproxy.cfg` with contents such as below:

To 'control' haproxy you need socat.
    brew install socat

```
global
  maxconn 4096
  #daemon

  # you can use the stats socket to send interactive commands to a running haproxy instance: echo "show stat" | socat stdio unix-connect:/tmp/haproxy.sock
  # to disable a server: echo "disable server onecontrol_backend/primary" | socat stdio unix-connect:/tmp/haproxy.sock

  stats socket    /tmp/haproxy.sock level admin


defaults
  log global
  log 127.0.0.1 local0
  log 127.0.0.1 local1 notice
  mode http
  timeout connect 300000
  timeout client 300000
  timeout server 300000
  maxconn 2000
  option redispatch
  retries 3
  option httpclose

frontend onecontrol_frontend
  bind *:9100
  default_backend onecontrol_backend
  # Add a header to each response so that BoD may detect which server we are talking to (and we can unsubscribe), for example: X-Backend-Server: primary localhost:9001
  rspadd X-Backend-Server:\ primary if { srv_id 1 }
  rspadd X-Backend-Server:\ secondary if { srv_id 2 }

backend onecontrol_backend
  balance roundrobin
  #replace the / below with the url for getting the wsdl
  option httpchk GET /mtosi/msi/ServiceInventoryRetrieval?wsdl
  # 145.145.64.78:9006 is primary
  server primary 145.145.64.78:9006 check

  #145.145.64.77:9006 is secondary
  server secondary 145.145.64.77:9006 backup check


listen haproxyapp_admin:9100 127.0.0.1:9101
  mode http
  stats uri /
```

Assuming you don't want to run haproxy as a daemon on your box, start haproxy as follows:
    haproxy -f path/to/your/edited/haproxy.cfg
