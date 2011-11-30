# Bandwidth on Demand (BoD)

## Getting the project to work
* The whole application is configured in a properties file called `bod.properties`
* The properties can be overridden in a `env/bod.properties` file
* Some properties are encrypted to be able to decrypt a `BOD_ENCRYPTION_PASSWORD` environment variable should be set

        export BOD_ENCRYPTION_PASSWORD=[password]

* To get the application working outside SURFnet you need to set the following properties

        idd.client.class = nl.surfnet.bod.idd.IddOfflineClient
        nbi.client.class = nl.surfnet.bod.nbi.NbiOfflineClient

