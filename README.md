-----------------AnyGuessr Backend------------

This is the backend service for the AnyGuessr application, built with Spring Boot and Maven.

Prerequisites

Before you begin, ensure you have the following installed on your machine:

Java Development Kit (JDK) (Java 25 recommended)

Git

Getting Started: Clone and Pull

1. Clone the Repository

If you haven't cloned the repository yet, open your terminal and run:
git clone https://github.com/NiallMahon/AnyGuessr.git
cd AnyGuessr

2. Pull the Latest Changes

To make sure your local copy is fully up to date with the main branch:
git checkout main
git pull origin main

How to Run the Backend Step-by-Step

Navigate into the backend directory from the project root:
cd backend

Option A: Run using the Maven Wrapper (Recommended)

You don't need to have Maven pre-installed on your system; the project includes a Maven wrapper script (mvnw).

On Linux / macOS:
./mvnw spring-boot:run

On Windows (Command Prompt / PowerShell):
mvnw.cmd spring-boot:run


Accessing the Application

Once the Spring Boot application starts successfully, open your browser or API client and go to:
http://localhost:8080
