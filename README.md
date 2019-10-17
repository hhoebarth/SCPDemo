# SCPDemo
This project shows how to set up a communication between microservices preserving user information on SAP Cloud Platform, Cloud Foundry using the SAP Cloud SDK. It includes two microservices with an app router per service.


  - docServices (with approuter-docServices): Microservice based on Spring Boot to merge XML data into an MS Word template including content controls.
    
  - rfcServices (with approuter-rfcServices): Microservice based on TomEE 7 to fetch an MS Word template and XML data for a given process order from an SAP backend via RFC call and merge those documents via HTTP call to the docServices Microservice.

Steps to setup scenario:
  - Download/Install Apache Maven http://maven.apache.org/download.cgi
  - Download/Install the Cloud Foundry CLI https://docs.cloudfoundry.org/cf-cli/install-go-cli.html
  - Create Trial account for Cloud Foundry on SAP Cloud Platform https://developers.sap.com/tutorials/hcp-create-trial-account.html
  - Download/Clone repository
  - Navigate into folder docServices
  - Build application using mvn clean install
  - logon to SAP Cloud Platform using cf login https://api.cf.eu10.hana.ondemand.com
  - create services on SAP Cloud Platform:
    - cf create-service xsuaa application xsuaa-docServices -c xs-security.json
    - cf create-service application-logs lite applog-docServices
  - replace \<<PUT IN YOUR SAP CP USER ID\>> in vars.yml with your SAP Cloud Platform user ID
  - push application to SAP Cloud Platform using cf push --vars-file vars.yml
  - Navigate into folder rfcServices
  - Build application using mvn clean install
  - create services on SAP Cloud Platform:
    - cf create-service xsuaa application conn-xsuaa -c xs-security.json
    - cf create-service destination lite destination-rfcServices
    - cf create-service application-logs lite applog-rfcServices
    - cf create-service connectivity lite connectivity-rfcServices
  - replace \<<PUT IN YOUR SAP CP USER ID\>> in vars.yml with your SAP Cloud Platform user ID
  - push application to SAP Cloud Platform using cf push --vars-file vars.yml
  - Logon to SAP Cloud Platform trial account in web browser
  - Add HTTP destination docServices to your Subaccount
    - Name: docServices
    - Type: HTTP
    - URL: <<URL to your docServices application on SAP Cloud Platform>>
    - ProxyType: Internet
    - Authentication: AppToAppSSO

If SAP ECC backend system is available:
  - Create Remote Function Z_PP_PRPP_GET_FILES with parameters
    - IMPORTING I_AUFNR type AUFNR
    - IMPORTING I_SPRAS type SPRAS
    - EXPORTING E_XML type STRING
    - EXPORTING E_DOCX type STRING
    This function module should export a Base64 encoded MS Word template (p.e. template.docx in rfcServices\src\main\webapp\WEB-INF) and a Base64 encoded xml data file (p.e. templateData.xml in rfcServices\src\main\webapp\WEB-INF). The files could be stored in the MIME repository and loaded from there using class CL_MIME_REPOSITORY. Base64 encoding could be done by using function module SSFC_BASE64_ENCODE.
  - Download/Install/Configure SAP Cloud Connector and connect to your Subaccount on SAP Cloud Platform and to SAP ECC backend https://blogs.sap.com/2017/07/09/how-to-use-the-sap-cloud-platform-connectivity-and-the-cloud-connector-in-the-cloud-foundry-environment-part-1/
  - Logon to SAP Cloud Platform trial account in web browser
  - Add RFC destination docServices to your Subaccount
    - Name: virtual host name of SAP ECC backend created in the last step
    - Type: RFC
    - User: User in SAP ECC backend
    - Password: Password in SAP ECC backend
    - Additional Properties:
      - jco.client.ashost: virtual host name of SAP ECC backend created in the last step
      - jco.client.client: client in SAP ECC backend
      - jco.client.sysnr: virtual port of SAP ECC backend created in the last step

