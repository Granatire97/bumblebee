# Threshold Alert Service (AKA Bumblebee)

A service that texts users when certain metrics cross thresholds for the DCSG Fulfillment Team.

README last updated: 8/01/2018

Spring Boot app that communicates with xMatters.
(Also known as Bumblebee)

## Getting Started

* Clone the repository using `git clone [repo_url]`.
* Open Spring Tool Suite and import the project by going to `File>Import>Projects from Folder or Archive` and opening the directory the project is in.
* After the threshold project is in your project explorer, right click on the the project and click 'Configure>Add Gradle Nature`.
* Create a folder called "libs" in the root directory of your project.
* Download the odjb7.jar from https://www.oracle.com/technetwork/database/features/jdbc/jdbc-drivers-12c-download-1958347.html and place the jar in the "libs" folder.
* Right click on the project and click on `Gradle>Refresh Gradle Project`.
* You should be able to run the project now, but nothing will work until you populate the application.yaml file in `src/main/resources/` or create a new application.yaml file in the root directory of the project.

## Project Architecture

#### Current Versioning
* Java 8
* Spring Boot 2.0.1
* xMatters 5.5.220

#### Spring Layout
```
com.dcsg.fulfillment.threshold
|- ThresholdApplication.java
|- ThresholdConfiguration.java
|-ThresholdController.java
|-ThresholdRepository.java
|-ThresholdService.java
|
|- resources
|   |-application.yaml
```
* __ThresholdApplication__: Class with main method that actually runs (nothing too important in here)
* __ThresholdConfiguration__: Reads config values from `application.yaml` and stores them as class fields.
* __ThresholdController__: Has the API endpoints and receives client requests to delegate to `ThresholdService`.
* __ThresholdRepository__: Has methods for querying databases for metric numbers/information.
* __ThresholdService__: Contains the logic for connecting `ThresholdRepository` with `ThresholdController`. In addition, makes API calls to xMatters to send texts when a metric's current value crosses a threshold from `application.yaml`
* __application.yaml__: Stores xMatters API endpoints, database connection configurations, and metric thresholds. If a metric's threshold needs to be updated, this is the place to do it.

#### xMatters Layout

The communication plan that the spring boot backend communicates with is currently called "Fulfillment Threshold Alert". Inside this communication plan, there is a form, inbound integration, property, and subscription form for every metric being monitored by the backend. 
***

## Testing

This project uses JUnit5 to test the Spring Boot backend. 

## Authors

* **Mark Granatire**
* **Tim Hartman**
* **Timotheus Hinton**
