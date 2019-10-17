# SCPDemo
This project shows how to set up a communication between microservices preserving user information on SAP Cloud Platform, Cloud Foundry using the SAP Cloud SDK. It includes two microservices with an app router per service.


  - docServices (with approuter-docServices): Microservice based on Spring Boot to merge XML data into an MS Word template including content controls.
    
  - rfcServices (with approuter-rfcServices): Microservice based on TomEE 7 to fetch an MS Word template and XML data for a given process order from an SAP backend via RFC call and merge those documents via HTTP call to the docServices Microservice.
