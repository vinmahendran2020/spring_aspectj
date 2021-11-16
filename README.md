### Project Name: ION Logging and Tracing ###

#####  Description:  ##### 
The Logging and Tracing Service will be built into a jar and be able to be used across other microservices as a dependency.


#### How to Build: ####
    1: Run mvn clean install.
    2: Run mvn deploy
    


#### Versions: ####
  OS: REHL v7  
  Springboot: 2.3.3.RELEASE  
  Java: Amazon Corretto v11  

#### Usage: ####
* At the moment you can test the functionality of the application by adding the repository and dependency in the pom of your desired microservice.  
* Information about the jar you have built will be under the target folder


#### Contributors: ####

Credits: 
  Initial creation by Anton Huang. Further development by Accenture's Project Ion Team
