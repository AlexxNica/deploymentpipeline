# Overview
Eco Pipeline is a component of the  Eco tool. Eco Pipeline provides application teams with a CI/CD orchestration platform to integrate automated delivery steps with manual steps such as decision-gate approvals. 

The Eco tool is an easy-to-use, self-service automation process to generate a new microService based on defined seeds/templates. Users can use the Eco tool from beginning to end of the microservice deployment lifecycle beginning with scaffolding the mS project and deploying it on Kubernetes through the Jenkins pipeline.

# Requirements
### Required Environment
* Linux

### Required Software
* Maven 3.x.x
* Java 8
* MariaDB

### Required Applications
* AAF
* CatalogAPI

### Other Requirements
* Jenkins Job Base
* Email Host
* Kubernetes Namespace


# Build
1. Install and configure Maven and Java. Many robust installation guides exist for both products and there is not a need for an additional guide here.
1. Clone the Eco Pipeline repository.
1. Install and configure MariaDB. Create the database schema by running pipelineschema.sql.
1. Follow the installation and deployment instructions for AAF ([here](https://github.com/att/AAF)) and CatalogAPI. Eco pipeline is dependent on both. Be sure to create a namespace in AAF.
1. Navigate to the project in a terminal and run `mvn install` using the settings.xml file provided with the project.

# Run
1. Configure Eco Pipeline by filling out the configuration files src/main/resources/system.properties, src/main/resources/application.properties,  src/main/resources/cadi.properties, and etc/cadi.properties. Fields are preset with default values, localhost addresses, or the dummy text "replaceme".
1. Generate a new keyfile and use it to replace the dummy file in etc/aafkeystore.
1. Create a run configuration with com.att.cicdpipeline.deploymentpipeline.Application as the main class
1. Add arguments `-Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true`
`-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true`
1. Launch with the run configuration.
1. The first time Eco Pipeline is run after creating the database schema, you must create a Camunda account. Use a browser to access http://hostofecopipeline:8888 (e.g. If running locally, navigate to http://localhost:8888). You will be redirected to an account creation page. The username MUST be demo and the password MUST be password. If you wish to use another user/password combination, you must edit the source code. You may give any values for the name and email fields. Confirm the account by signing in after submitting. This process does not need to be repeated unless the database is reset. Eco Pipeline is now ready to be used.

# NOTE
More details can be found in the wiki for this project ([here](https://github.com/att/deploymentpipeline/wiki))