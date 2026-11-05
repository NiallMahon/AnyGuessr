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

Database on AWS (RDS)

Current live mySQL database runs on Amazon RDS in eu-north-1 (Stockholm). To avoid campus WIFI blocking the port, you connect through an SSH tunnel on port 443.

One-time setup

1. Create a file called .env.aws in the repo root with the values you were sent privately:
   MYSQL_DATABASE=anyguessr
   MYSQL_USER=anyguessr_app
   MYSQL_PASSWORD=<the app password>
   DB_HOST=127.0.0.1
2. On campus only: you need the SSH key you registered for the relay, at ~/.ssh/anyguessr-relay.
   To make one: ssh-keygen -t ed25519 -f ~/.ssh/anyguessr-relay (share only the .pub file).

For connecting on Campus WIFI:

1. Open the tunnel in its own terminal and leave it open:
   ssh -i ~/.ssh/anyguessr-relay -p 443 -N -L 127.0.0.1:3306:<RDS endpoint>:3306 ec2-user@<relay IP>
2. Keep DB_HOST=127.0.0.1 in .env.aws.
3. Run the backend as usual (mvnw.cmd spring-boot:run, or Run in IntelliJ).

Off campus:

1. No tunnel. In .env.aws, set DB_HOST to the RDS endpoint.
2. Your home IP must be on the allowed list (check it at https://checkip.amazonaws.com).
3. Run the backend as usual.

The backend reads .env.aws by itself. The log line "Database version: 8.4.11" means you are on the shared database. Without .env.aws, it uses the local Docker database as before.

Common problems
- "Permission denied" on the tunnel, or "Port 8080 was already in use": something else is using that port (often a local Docker MySQL or a backend that is still running). Stop it and try again.
- "Connection timed out": off campus, your IP isn't allowed yet; on campus, check the tunnel window is still open.

Setting up a new database: run every file in db/init in order, then db/seed/demo.sql (the demo player, the World map and its three quiz regions).
