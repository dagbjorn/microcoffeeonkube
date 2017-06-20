# microcoffeeonkube - The &micro;Coffee Shop on Kubernetes

## Revision log

Date | Change
---- | -------
20.06.2017 | Created.

## Contents

* [Acknowledgements](#acknowledgements)
* [The application](#application)
* [Prerequisite](#prerequisite)
* [Start Minikube](#start-minikube)
* [Building microcoffee](#building-microcoffee)
* [Application and environment properties](#properties)
* [Run microcoffee](#run-microcoffee)
* [Setting up the database](#setting-up-database)
* [Give microcoffee a spin](#give-a-spin)
* [REST services](#rest-services)
* [Other stuff](#other-stuff)

## <a name="acknowledgements"></a>Acknowledgements
The &micro;Coffee Shop application is based on the coffee shop application coded live by Trisha Gee during her fabulous talk, "HTML5, Angular.js, Groovy, Java, MongoDB all together - what could possibly go wrong?", given at QCon London 2014. A few differences should be noted however; microcoffee uses a microservice architecture, runs in Docker containers on Kubernetes - the open source platform for management of containerized applications - and is developed in Spring Boot instead of Dropwizard as in Trisha's version.

## <a name="application"></a>The application

### The microservices
The application is made up by four microservices, each running in its own Docker container. Each microservice, apart from the database, is implemented by a Spring Boot application. Finally, the application is deployed on Kubernetes where each microservice is allocated a replication controller fronted by a service. For development purposes, each microservice may also be run as a "naked" pod. Endpoint configuration is based on environment variables.

The application supports both http and https on the frontend consisting of a GUI and REST services facing the Internet. However, https is a requirement in Chrome and Opera to get the HTML Geolocation API going. Also, browsers are not particulary happy with mixed content (mix of http and https connections), so pure use of https is recommended.

#### microcoffeeonkube-database
Contains the MongoDB database. The database image is based on the [tutum/mongodb](https://hub.docker.com/r/tutum/mongodb/) image on DockerHub.

The database installation uses a persistent Kubernetes volume, *mongodbdata*, for data storage. This volume is automatically created by Kubernetes according to the configuration in the manifest file.

:warning: The database runs without any security enabled.

#### microcoffeeonkube-location
Contains the Location REST service for locating the nearest coffee shop. Coffee shop geodata is downloaded from [OpenStreetMap](https://www.openstreetmap.org) and imported into the database.

:bulb: The `microcoffeeonkube-database` project contains a geodata file, `oslo-coffee-shops.xml`, with all Oslo coffee shops currently registered on OpenStreetMap. See [Download geodata from OpenStreetMap](#download-geodata) for how this file is created.

#### microcoffeeonkube-order
Contains the Menu and Order REST services. Provides APIs for reading the coffee menu and placing coffee orders.

#### microcoffeeonkube-gui
Contains the application GUI written in AngularJS. Nothing fancy, but will load the coffee shop menu from which your favorite coffee drink may be ordered. The user may also locate the nearest coffee shop and show it on Google Maps.

### Common artifacts
The application also contains common artifacts (for the time being only one) which are used by more than one microservice. Each artifact is built by its own Maven project.

A word of warning: Common artifacts should be used wisely in a microservice architecture.

#### microcoffeeonkube-certificates
Creates a self-signed PKI certificate, contained in the Java keystore `microcoffee-keystore.jks`, needed by the application to run https. In fact, two certificates are created, one with the fixed common name (CN) `localhost` and one with a common name free of choice (default `192.168.99.100`).

### microcoffeeonkube-kubernetes
Contains the Kubernetes manifest files (yaml) of the application, one manifest file per microservice. For each microservice, a replication controller is created which is fronted by a service.

Convenience batch files for easy deploy and undeploy of the application are also provided.

## <a name="prerequisite"></a>Prerequisite
The microcoffee application is developed on Windows 10 and tested with Minikube 0.19.1 running on Oracle VM VirtualBox 5.1.22.

For building and testing the application on a development machine, Minikube is a good option. Minikube runs a single-node Kubernetes cluster inside a VM on your development machine. In addition, kubectl - the Kubernetes CLI - must be installed.

Installation of Minikube and kubectl is straightforward, just download the two executables and place them in a folder on your Windows path. You may also want to define the two environment variables MINIKUBE\_HOME and KUBECONFIG to suitable folders if you don't like the default locations in your user home directory. MINIKUBE\_HOME is where the VM is downloaded first time you start Minikube. The size of this folder will grow over time. KUBECTL contains the kubectl config file.

A separate Docker installation on your development machine is also useful, however strictly not necessary.

In addition, you need the basic Java development tools (IDE w/ Java 1.8 and Maven) installed on your development machine.

## <a name="start-minikube"></a>Start Minikube
Before moving on and start building microcoffee, we need a running VM. The reason is that the Docker images being built are stored in the local Docker repository inside the VM.

To start Minikube, i.e. the local Kubernetes cluster, run:

    minikube start

Next, configure the Docker environment variables in your shell following the instructions displayed by:

    minikube docker-env

:bulb: On Windows, it is handy to create a batch file to do this. The file should be placed in a folder on your Windows path. A sample batch file, `minikube-setenv.bat`, is located in the `utils` folder of `microcoffeeonkube-kubernetes`.

To check the status of your local Kubernetes cluster, run:

    minikube status

## <a name="building-microcoffee"></a>Building microcoffee

### Get the code from GitHub
Clone the project from GitHub, https://github.com/dagbjorn/microcoffeeonkube.git, or download the zip file and unzip it.

### Build common artifacts

#### Create the certificate artifact
In order for https to work, a self-signed certificate needs to be created. The `microcoffeeonkube-certificates` project builds a jar containing a Java keystore, `microcoffee-keystore.jks`, with the following two certificates:

* One certificate for use on `localhost`, i.e. common name is set to this value.
* One certificate for use on a user-defined hostname/IP address (default value is `192.168.99.100`).

The key alias is set to the same value as the common name.

In `microcoffeeonkube-certificates`, run:

    mvn clean install

To inspect the created keystore, run:

    keytool -list -v -keystore target\classes\microcoffee-keystore.jks -storepass 12345678

To specify, a different common name and/or key alias, run:

    mvn clean install -Dcn=myhost.com
    mvn clean install -Dcn=myhost.com -Dalias=mykey

:bulb: The keystore properties are specified in `application.properties` of each microservice using the `microcoffeeonkube-certificates` artifact.

### Build the microservices
Use Maven to build each microservice in turn. (The Spring Boot applications only.)

:exclamation: Just remember that Minikube must be running for building the Docker images successfully.

In `microcoffeeonkube-location`, `microcoffeeonkube-order` and `microcoffeeonkube-gui`, run:

    mvn clean package docker:build

## <a name="properties"></a>Application and environment properties
Application and environment-specific properties are defined in the following files:

Project | Production | Integration testing
------- | ---------- | -----------------
microcoffeeonkube-gui | env.js | n/a
microcoffeeonkube-location | application.properties | application-test.properties
microcoffeeonkube-order | application.properties | application-test.properties

Environment-specific properties comprise:
* Database connection URL (for integration testing, separate properties are used).
* REST service URLs.
* Keystore properties.

In particular, you need to pay attention to the IP address of the VM. Default value used by the application is **192.168.99.100**. (Suits VirtualBox.)

The port numbers are:

Microservice | http port | https port | Comment
------- | ---------- | ---------- | ---------
microcoffeeonkube-gui | 9080 | 9443 | Port 8443 is allocated by Kubernetes itself.
microcoffeeonkube-location | 8081 | 8444 |
microcoffeeonkube-order | 8082 | 8445 |
microcoffeeonkube-database | 27017 | n/a |

:warning: If you change any of the environment properties, you need to rebuild the actual Docker image.

In a production environment (here that means the application deployed to Minikube), the database connection URL and REST service URLs are defined by environment variables. The table below defines the environment variables that must be defined per microservice.

Microservice | Environment variable | Defined by
------------ | -------------------- | ------------
microcoffeeonkube-gui | MICROCOFFEE\_WEB\_HOST | Manifest file
microcoffeeonkube-gui | MICROCOFFEE\_WEB\_PORT\_LOCATION\_HTTP | Manifest file
microcoffeeonkube-gui | MICROCOFFEE\_WEB\_PORT\_LOCATION\_HTTPS | Manifest file
microcoffeeonkube-gui | MICROCOFFEE\_WEB\_PORT\_ORDER\_HTTP | Manifest file
microcoffeeonkube-gui | MICROCOFFEE\_WEB\_PORT\_ORDER\_HTTPS | Manifest file
microcoffeeonkube-location | MICROCOFFEE\_MONGODB\_SERVICE\_HOST | Kubernetes service (out of the box)
microcoffeeonkube-location | MICROCOFFEE\_MONGODB\_SERVICE\_PORT | Kubernetes service (out of the box)
microcoffeeonkube-order | MICROCOFFEE\_MONGODB\_SERVICE\_HOST | Kubernetes service (out of the box)
microcoffeeonkube-order | MICROCOFFEE\_MONGODB\_SERVICE\_PORT | Kubernetes service (out of the box)

Note that `env.js` is filtered by a special servlet (`EnvironmentFilterServlet`) in the gui microservice, substituting the environment variables used by the REST endpoint URLs by actual values.

## <a name="setting-up-database"></a>Setting up the database

### The Kubernetes volume used by the MongoDB database
The manifest file `microcoffee-mongodb.yml` creates a Kubernetes volume of type hostPath to be used by the MongoDB database. The properties of the volume are as follows:

* Name: `mongodbdata`
* mountPath (inside the container): `/data/db`
* hostPath (on the VM): `/mnt/sda1/data/mongodbdata`

The data stored in the volume survive restarts. This is because Minikube by default is configured to persist files stored in `/data` (a softlink to `/mnt/sda1/data`).


### Load data into the database collections
The `microcoffeeonkube-database` project is used to load coffee shop locations, `oslo-coffee-shops.xml`, and menu data into a database called  *microcoffee*. This is accomplished by running the below Maven command. (We run it twice to also load the test database, *microcoffee-test*.) Make sure to specify the correct IP address of your VM.

But first we need to start MongoDB. From the `microcoffeeonkube-database` project, run:

    deploy-k8s.bat

This is a convenience batch file creating a pod containing the mongodb container. When the status of the pod is running, press Ctrl-C to terminate the batch file.

Then run:

    mvn gplus:execute -Ddbhost=192.168.99.100 -Ddbport=27017 -Ddbname=microcoffee -Dshopfile=oslo-coffee-shops.xml
    mvn gplus:execute -Ddbhost=192.168.99.100 -Ddbport=27017 -Ddbname=microcoffee-test -Dshopfile=oslo-coffee-shops.xml

:warning: Just ignore the `java.security.AccessControlException` warnings that are thrown due to failed MBean registration.

To verify the database loading, start the MongoDB client inside the *mongodb* container. (This command is also echoed by `deploy-k8s.bat`).

    kubectl exec -it microcoffee -c mongodb -- mongo microcoffee

    > show databases
    admin             0.000GB
    local             0.000GB
    microcoffee       0.000GB
    microcoffee-test  0.000GB
    > show collections
    coffeeshop
    drinkoptions
    drinksizes
    drinktypes
    > db.coffeeshop.count()
    93
    > db.coffeeshop.findOne()
    {
            "_id" : ObjectId("58610703e113eb24f46a97a8"),
            "openStreetMapId" : "292135703",
            "location" : {
                    "coordinates" : [
                            10.7587531,
                            59.9234799
                    ],
                    "type" : "Point"
            },
            "addr:city" : "Oslo",
            "addr:country" : "NO",
            "addr:housenumber" : "55",
            "addr:postcode" : "0555",
            "addr:street" : "Thorvald Meyers gate",
            "amenity" : "cafe",
            "cuisine" : "coffee_shop",
            "name" : "Kaffebrenneriet",
            "opening_hours" : "Mo-Fr 07:00-19:00; Sa-Su 09:00-17:00",
            "operator" : "Kaffebrenneriet",
            "phone" : "+47 95262675",
            "website" : "http://www.kaffebrenneriet.no/butikkene/butikkside/kaffebrenneriet_thorvald_meyersgate_55/",
            "wheelchair" : "no"
    }
    >

Finally, delete the mongodb pod by executing another convenience batch file in the project folder:

    undeploy-k8s.bat

## <a name="run-microcoffee"></a>Run microcoffee
From the `service` folder of `microcoffeeonkube-kubernetes`, deploy microcoffee on Kubernetes using the following convenience batch file:

    deploy-k8s-all.bat

The batch file creates the replication controller and service of each of the four microservices. (Run `undeploy-k8s-all.bat`to undeploy.)

Each project contains its own `microcoffee-pod.yml` which will create a pod containing all downstream containers in addition to itself.

For testing individual projects outside Kubernetes, run:

    run-local.bat

This is a batch file that defines the necessary environment variables needed by the microservice before running `mvn spring-boot:run`.

## <a name="give-a-spin"></a>Give microcoffee a spin
After microcoffee has started (it takes a while), navigate to the coffee shop to place your first coffee order:

    https://192.168.99.100:9443/coffee.html

assuming the VM host IP 192.168.99.100 and that https is in use.

:warning: Because of the self-signed certificate, a security-aware browser will complain a bit.
* Chrome: Just click Advanced and hit the "Proceed to 192.168.99.100 (unsafe)" link.
* Opera: Just click Continue anyway.
* Firefox: Takes a bit more effort. Click Advanced, then Add Exception and finally Confirm Security Exception. Make sure that "Permanently store this exception" is checked. Next, manually add exceptions for the REST service URLs.
  - Select Tools > Options > Advanced > Certificates > View Certificates to open Certificate Manager.
  - In turn, add **Server** exceptions for the following locations: `https://192.168.99.100:8444` and `https://192.168.99.100:8445`
  - Click Get Certificate
  - Click Confirm Security Exception (make sure that "Permanently store this exception" is checked).

:no_entry: The application doesn't work in IE11. Error logged in console: "Object doesn't support property or method 'assign'." Object.assign is used in coffee.js. Needs some fixing... And Microsoft Edge? Cannot even find the site...

## <a name="rest-services"></a>REST services

### APIs

* [Location API](#location-api)
* [Menu API](#menu-api)
* [Order API](#order-api)


### <a name="location-api"></a>Location API

#### Get nearest coffee shop

**Syntax**

    GET /coffeeshop/nearest/{latitude}/{longitude}/{maxdistance}

Find the nearest coffee shop within *maxdistance* meters from the position given by the WGS84 *latitude*/*longitude* coordinates.

**Response**

HTTP status | Description
----------- | -----------
200 | Coffee shop found. The name, location etc. is returned in JSON-formatted HTTP response body.
204 | No coffee shop found within specified distance from given position.

**Example**

Find the coffee shop closest to the Capgemini Skøyen office:

    GET http://192.168.99.100:8081/coffeeshop/nearest/59.920161/10.683517/200

Response:

    {
      "_id": {
        "timestamp": 1482086231,
        "machineIdentifier": 5422646,
        "processIdentifier": 19508,
        "counter": 9117700,
        "time": 1482086231000,
        "date": 1482086231000,
        "timeSecond": 1482086231
      },
      "openStreetMapId": "428063059",
      "location": {
        "coordinates": [
          10.6834023,
          59.920229
        ],
        "type": "Point"
      },
      "addr:city": "Oslo",
      "addr:country": "NO",
      "addr:housenumber": "22",
      "addr:postcode": "0278",
      "addr:street": "Karenslyst Allé",
      "amenity": "cafe",
      "cuisine": "coffee_shop",
      "name": "Kaffebrenneriet",
      "opening_hours": "Mo-Fr 07:00-18:00; Sa-Su 09:00-17:00",
      "operator": "Kaffebrenneriet",
      "phone": "+47 22561324",
      "website": "http://www.kaffebrenneriet.no/butikkene/butikkside/kaffebrenneriet_karenslyst_alle_22/"
    }

**Testing with curl**

    curl -i http://192.168.99.100:8081/coffeeshop/nearest/59.920161/10.683517/200
    curl -i --insecure https://192.168.99.100:8444/coffeeshop/nearest/59.920161/10.683517/200

:bulb: For testing with https, use a recent curl version that supports SSL. (7.46.0 is good.)

CORS testing: Adding an Origin header in the request should return Access-Control-Allow-Origin in the response.

    curl -i -H "Origin: http://192.168.99.100:8080" http://192.168.99.100:8081/coffeeshop/nearest/59.920161/10.683517/200
    curl -i --insecure -H "Origin: https://192.168.99.100:8443" https://192.168.99.100:8444/coffeeshop/nearest/59.920161/10.683517/200

### <a name="menu-api"></a>Menu API

#### Get menu

**Syntax**

    GET /coffeeshop/menu

Get the coffee shop menu.

**Response**

HTTP status | Description
----------- | -----------
200 | Menu returned in JSON-formatted HTTP response body.

**Example**

    GET http://192.168.99.100:8082/coffeeshop/menu

Response (abbreviated):

    {
        "types": [
            {
                "_id": {
                    "timestamp": 1482086232,
                    "machineIdentifier": 5422646,
                    "processIdentifier": 19508,
                    "counter": 9117791,
                    "time": 1482086232000,
                    "date": 1482086232000,
                    "timeSecond": 1482086232
                },
                "name": "Americano",
                "family": "Coffee"
            },
            ..
        ],
        "sizes": [
            {
                "_id": {
                    "timestamp": 1482086232,
                    "machineIdentifier": 5422646,
                    "processIdentifier": 19508,
                    "counter": 9117795,
                    "time": 1482086232000,
                    "date": 1482086232000,
                    "timeSecond": 1482086232
                },
                "name": "Small"
            },
            ..
        ],
        "availableOptions": [
            {
                "_id": {
                    "timestamp": 1482086232,
                    "machineIdentifier": 5422646,
                    "processIdentifier": 19508,
                    "counter": 9117800,
                    "time": 1482086232000,
                    "date": 1482086232000,
                    "timeSecond": 1482086232
                },
                "name": "soy",
                "appliesTo": "milk"
            },
            ..
        ]
    }

**Testing with curl**

    curl -i http://192.168.99.100:8082/coffeeshop/menu
    curl -i --insecure https://192.168.99.100:8445/coffeeshop/menu

### <a name="order-api"></a>Order API

#### Place order

**Syntax**

    POST /coffeeshop/{coffeeShopId}/order

Place an order to the coffee shop with ID *coffeeShopId*. The order details are given in the JSON-formatted HTTP request body.

The returned Location header contains the URL of the created order.

**Response**

HTTP status | Description
----------- | -----------
201 | New order created.

**Example**

    http://192.168.99.100:8082/coffeeshop/1/order

    {
        "coffeeShopId": 1,
        "drinker": "Dagbjørn",
        "size": "Small",
        "type": {
            "name": "Americano",
            "family": "Coffee"
        },
        "selectedOptions": [
            "decaf"
        ]
    }

Response:

    {
        "id": "585fe5230d248f00011173ce",
        "coffeeShopId": 1,
        "drinker": "Dagbjørn",
        "size": "Small",
        "type": {
            "name": "Americano",
            "family": "Coffee"
        },
        "selectedOptions": [
            "decaf"
        ]
    }

**Testing with curl**

:white_check_mark: Must be run from `microcoffee-order` to find the JSON file `src\test\curl\order.json`.

    curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d @src\test\curl\order.json http://192.168.99.100:8082/coffeeshop/1/order
    curl -i --insecure -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d @src\test\curl\order.json https://192.168.99.100:8445/coffeeshop/1/order

#### Get order details

**Syntax**

    GET /coffeeshop//{coffeeShopId}/order/{orderId}

Read the details of the order of the given ID *orderId* from coffee shop with the given ID coffeeShopId.

**Response**

HTTP status | Description
----------- | -----------
200 | Order found and return in the JSON-formatted HTTP response body.
204 | Requested order ID is not found.

**Example**

    http://192.168.99.100:8082/coffeeshop/1/order/585fe5230d248f00011173ce

Response:

    {
        "id": "585fe5230d248f00011173ce",
        "coffeeShopId": 1,
        "drinker": "Dagbjørn",
        "size": "Small",
        "type": {
            "name": "Americano",
            "family": "Coffee"
        },
        "selectedOptions": [
            "decaf"
        ]
    }

**Testing with curl**

    curl -i http://192.168.99.100:8082/coffeeshop/1/order/585fe5230d248f00011173ce
    curl -i --insecure https://192.168.99.100:8445/coffeeshop/1/order/585fe5230d248f00011173ce

## <a name="other-stuff"></a>Other stuff

### <a name="download-geodata"></a>Download geodata from OpenStreetMap

:construction: Just some old notes for now...

Download geodata:
- Go to https://www.openstreetmap.org
- Search for Oslo
- Select a search result
- Adjust wanted size of map
- Click Export
- Click Overpass API (works better)
- Save to file: `oslo.osm`

osmfilter:
http://wiki.openstreetmap.org/wiki/Osmfilter
osmfilter is used to filter OpenStreetMap data files for specific tags.

Install dir for exe (Windows): `C:\apps\utils`

List of all Keys, sorted by Occurrence:

    osmfilter oslo.osm --out-count

List of a Key's Values, sorted by Occurrence:

    osmfilter oslo.osm --out-key=cuisine | sort /r

Get all coffee shops:

    osmfilter oslo.osm --keep="all cuisine=coffee_shop" > oslo-coffee-shops.xml
