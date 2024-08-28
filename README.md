# playlists

## Purpose

Compile Spotify playlists from music charts published on the internet. At present the Dutch Top40 and Tipparade are supported.

## Background

Like most people, I love music. But the last couple of years I hardly listen to the radio anymore, so I was lacking input of new music being released.

And like some people, I'm kind of a nerd. So one day, I started to compile a playlist on Spotify per year of all music released in that year. By hand that is. I went over the charts at top40.nl (yes, I am Dutch) and for each week, for every new entry, I looked up the song on spotify, and added it to the playlist. When I completed an annum, I would listen to all the songs in the playlist and rate them. Each song I liked, I added to my playlist called "Nice", and when I thought a song was awesome, I added it to a playlist called "Good and better" instead. Those two are my main playlists for listening to mainstream pop music.

I timed my efforts to compile the playlist on spotify and I estimated that it took me about 4 hours per quarter, that is 16 hours per year, to create a playlist. I have done this now for ten volumes of the top40, and the nerd in me wants to be complete, so I have a couple of more volumes to go. That amounts to a really large number of hours compiling playlists, which, I thought, I could spent in better ways.

Thus, one day, I decided to automate the process and this little program is the result.

## Implementation

top40.nl does not provide an API to get the weekly charts in an automated fashion. So this program reads the html pages
from internet, extracts the relevant information from it and stores it in a database. Then it can compile a list of all
releases in a certain year, create a Spotify playlist and fill it with as much of the songs it can find on Spotify.

Previously, the application was a commandline tool and had no Graphical User Interface for human interaction. This changed in August 2024, when I added a browser application that can be accessed via your favourite webbrowser, like Chrome, Firefox, Edge or Safari. The GUI should provide you more control and insight over the previous commandline tool. It is a first incarnation and hopefully I have some time in the future to expand it.

It is probable that the application can not find all released songs on Spotify. This can occur when the artist / title from the music charts use a different spelling than Spotify. Sometimes they have a different attribution. Or there are too many 'noice words' in either title or artist names. But it is also possible that the song is not available on Spotify. You can try to manually find them yourself in the Spotify application and add them to your playlist by hand.

When you instruct the program multiple times to compile the playlist for the same year, it will never remove songs from the existing playlist, it will only add new songs when they are not already present.

## Run the demo on the internet

The application is running on the internet. Go to the web address [https://playlists.rsdev.info](https://playlists.rsdev.info) to see what it's all about.

_Please note a quirk during login:  
When you connect to spotify, you stay on the /connect-page, after providing credentials to Spotify. You have to click the spotify icon again to go to the /main page. This will be solved in a future release._

## Run it on your own machine

The application is distributed as source code. What you need to get this program running on your machine, is the following:

1. A git client to retrieve the source code
1. A Java 21 JDK (or newer) to compile the source code to executable code

plus

3. A running MariaDb database (for data storage that survives reboots)

or

3. Docker (for data storage that lasts as long as you keep the application running) \*)

and optionally

4. A clientId, in case you have your own application registration with Spotify

In the next sections, each requirement is discussed in more detail.

_**Footnote:**_  
_\*) If you know docker well, you probably know ways to use docker with persistent storage that survives reboots_

### A git client

The Playlists application is currently only distributed as source code on [GitHub](https://github.com/dschoorl/playlists) (You know where) Use your favourite git client to clone the repository on your local machine. When you use the git commandline, it may look something like this:

```
git clone https://github.com/dschoorl/playlists.git
```

This will create a subdirectory in your current directory with the source code. This subdirectory is refered to as 'the project root directory'.

### Java 21 JDK or newer

you know if you have Java JDK version 21 or newer installed, when you get an valid answer from the following command in the terminal:

```
javac -version
```

If it cannot find javac or the version number it displays is lower than 21, you have work to do. It is beyond the scope of this `readme` to give instructions, but if you cannot progress after searching the internet for information on how to install JDK 21 on you Operating System, this might help: [https://adoptium.net/](https://adoptium.net/)

### A running MariaDb database

When I started the project, I choose a NoSQL database over a relational one, only for me to gain some experience with this type
of database. However, it is not a good fit for this application, so I decided to change it back to a relational one. Since September 2023
it will use a MariaDb relational database.

If you not already have a MariaDb server running (MySql should work as well), please install one. _Please note: you can run the application without installing mariadb on your computer by using docker. Instructions for docker are provided later in this readme._

You can find installation instructions
for windows [here](https://www.mariadbtutorial.com/getting-started/install-mariadb/) and for Linux
[here](https://www.digitalocean.com/community/tutorial-collections/how-to-install-mariadb). I have tested the application against MariaDb version
10.11. The application uses Liquibase to update the table structure over time. You must only create the database yourself plus create two
accounts, one with rights to maintain the data and one with the rights to maintain the datbase structure. You could use the sql below for
inspiration (read: change the passwords to something only you know):

```sql
create database if not exists playlists;

create user if not exists 'liquibase'@'%' identified by 'liquibase';
grant all privileges on playlists.* to 'liquibase'@'%';

create user if not exists 'pl_user'@'%' identified by 'pl_user';
grant delete, execute, insert, select, update on playlists.* to 'pl_user'@'%';

flush privileges;
```

Next you need to provide the database credentials to the playlists application via a properties file that lives next to the executable jar file. We call it `application-local.properties` and it should contain the following properties:

```properties
spring.liquibase.password=<password_of_liquibase>
spring.datasource.password=<password_of_pl_user>
```

You must substitute these dummy passwords with the values you have used when you created the accounts in mariadb.

### Docker

Docker is a tool to virtualize an applications' runtime environment. It has many benefits, one of them being the ability to run an application with minimum setup effort by the user.

Docker is distributed for all major operating systems. If you not already have it on your computer, you can find installation instructions [here](https://docs.docker.com/get-started/get-docker/)

### A Spotify clientId

This application will exchange data with Spotify on your behalve. That means you must provide your credentials to Spotify and grant the application the rights to create and modify playlists for you.

I have registered the application with Spotify and received a client id and a client secret, that proves that I am the registration holder for the playlists application. The client id is needed to exchange data with Spotify, and it is included in the source code. This is fine for you, unless you want to deviate from my setup: e.g. run on a different TCP port or on a host other than localhost. In that case, you must tell Spotify to use another redirectUrl, reflecting your port and host name. And that means you must make your own registration with Spotify and get your own client id.

If you will use my setup instructions, you can stop reading this section and move on to the next: [Building and running](#building-and-running).

Still reading? That means you want to create your own client id with Spotify.

First complete the registration process (see steps below) to obtain a client id. Then I will tell you how to provide it to the application

- Go to the Spotify developers website's dashboard at https://developer.spotify.com/
- Click on login and use an existing Spotify account or create a new (developer) account
- When required, accept the license and/or verify your email address
- From the dashboard, you can click the 'create app' button
- Choose the name and description so you know what it is for so you will recognize it's purpose in the future.
- Fill out the URI that Spotify will use to redirect to after login. The playlists application expects something like http[s]://hostname[:port]/connect. You can choose anything before /connect, but the application expects the path /connect. You can supply multiple URI's and you can change them lateron, if they turn out to be incorrect.  
  I myself have the following URI's registered:
  1. http://localhost:8080/connect (for running as java application)
  1. http://localhost:8888/connect (for running with docker)
  1. https://playlists.rsdev.info/connect (for my online demo)
- When finished, you are taken to the details of the newly created Spotify app. There you see the ClientId. Copy it, you need this later on.
- You are now done registering the application with Spotify. If you like you can logout.

Next, we need to change the client id in the source code and replace it with your's.

From the project root folder, edit two files on the path `angular-client/src/environments/environment.ts` and `angular-client/src/environments/environment.development.ts`, so that the value of `spotifyClientId` contains your client id (must be between quotes, because it is a string value).

Now you are ready to build and run the application with your client id.

### Building and running

The project uses Gradle as build tool. You do not need to have Gradle installed prior to building the project, because the right version will be download automatically.

Execute the following command to build the project from the project root:  
On Windows: `gradelw.bat build `  
On Mac/Linux: `./gradlew build`

This will create a fat jar in the sub directory called `playlist-server/build/libs` that can be executed with the `java -jar` command:

On Windows: `java -jar playlist-server\build\libs\playlists.jar --spring.profiles.active=local`  
On Mac/Linux: `java -jar playlist-server/build/libs/playlists.jar --spring.profiles.active=local`

You are now running the application as a Java process. You will see the application log that looks something similar like the one below:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.2)

2024-08-28 10:55:34 INFO  info.rsdev.playlists.Playlists [StartupInfoLogger.java:50] - Starting Playlists v0.1.0-SNAPSHOT using Java 21.0.4 with PID 87779 (/home/dschoorl/projects/playlists/playlist-server/build/libs/playlists.jar started by dschoorl in /home/dschoorl/projects/playlists)
2024-08-28 10:55:34 INFO  info.rsdev.playlists.Playlists [SpringApplication.java:660] - The following 1 profile is active: "local"
2024-08-28 10:55:35 INFO  o.s.b.w.e.tomcat.TomcatWebServer [TomcatWebServer.java:111] - Tomcat initialized with port 8080 (http)
2024-08-28 10:55:35 INFO  o.a.catalina.core.StandardService [DirectJDKLog.java:173] - Starting service [Tomcat]
2024-08-28 10:55:35 INFO  o.a.catalina.core.StandardEngine [DirectJDKLog.java:173] - Starting Servlet engine: [Apache Tomcat/10.1.26]
2024-08-28 10:55:35 INFO  o.a.c.c.C.[Tomcat].[localhost].[/] [DirectJDKLog.java:173] - Initializing Spring embedded WebApplicationContext
2024-08-28 10:55:35 INFO  o.s.b.w.s.c.ServletWebServerApplicationContext [ServletWebServerApplicationContext.java:296] - Root WebApplicationContext: initialization completed in 1010 ms
2024-08-28 10:55:35 INFO  o.s.b.a.w.s.WelcomePageHandlerMapping [WelcomePageHandlerMapping.java:59] - Adding welcome page: class path resource [static/index.html]
2024-08-28 10:55:36 INFO  liquibase.ui [JavaLogger.java:37] - WARNING:

Liquibase detected the following invalid LIQUIBASE_* environment variables:

- LIQUIBASE_SECRET

Find the list of valid environment variables at https://docs.liquibase.com/environment-variables

2024-08-28 10:55:36 WARN  liquibase.configuration [JavaLogger.java:37] -

Liquibase detected the following invalid LIQUIBASE_* environment variables:

- LIQUIBASE_SECRET

Find the list of valid environment variables at https://docs.liquibase.com/environment-variables

2024-08-28 10:55:36 INFO  liquibase.changelog [JavaLogger.java:37] - Reading from playlists.DATABASECHANGELOG
2024-08-28 10:55:36 INFO  liquibase.ui [JavaLogger.java:37] - Database is up to date, no changesets to execute
2024-08-28 10:55:36 INFO  liquibase.changelog [JavaLogger.java:37] - Reading from playlists.DATABASECHANGELOG
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - UPDATE SUMMARY
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - Run:                          0
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - Previously run:               2
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - Filtered out:                 0
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - -------------------------------
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - Total change sets:            2
2024-08-28 10:55:36 INFO  liquibase.util [JavaLogger.java:37] - Update summary generated
2024-08-28 10:55:36 INFO  liquibase.lockservice [JavaLogger.java:37] - Successfully released change log lock
2024-08-28 10:55:36 INFO  liquibase.command [JavaLogger.java:37] - Command execution complete
2024-08-28 10:55:37 INFO  o.s.b.w.e.tomcat.TomcatWebServer [TomcatWebServer.java:243] - Tomcat started on port 8080 (http) with context path '/'
2024-08-28 10:55:37 INFO  info.rsdev.playlists.Playlists [StartupInfoLogger.java:56] - Started Playlists in 3.25 seconds (process running for 3.8)

```

The application is now running and you can access the GUI from the browser via the url [http://localhost:8080](http://localhost:8080). From this browser application you can create playlists on spotify with releases of a specific year.

The application contains data untill August 2024 for the Top40 and Tipparade that is loaded into the database if you run it for the first time. A task is scheduled to run each thursday at 10:00 PM to update the data from the internet.

You can stop the application with Ctrl-C.

## Running with docker

If you want to run with docker, you still have to buid the source code as mentioned above, but you start the application differently. You have to perform the following steps from the commmandline:

Execute the following command to build the project from the project root:  
On Windows: `gradelw.bat build `  
On Mac/Linux: `./gradlew build`

Go into the docker subdirectory and execute the following command to start mariadb and the playlists application:

```
docker compose up --build
```

This will start the application and leave the terminal window open to follow the log file.

It may take a minute to start, because docker starts with an empty database and it will load data (uptill August 2024) every time you start it. When it's ready, the logfile will end with the message like `Completed initialization in 0 ms`. After that, you can access the GUI from your browser at [http://localhost:8888](http://localhost:8888) (Yes, it's a different port compared to running the application as Java process)

You can stop the application with Ctrl-C and to clean up, you can type (still from the docker directory):

```
docker compose down
```

## Status

The program works for me, because I know how to handle and run it. The GUI makes it easier to use for the uninitiated, but it is not (yet) 'commercial grade' quality.

I tried to explain setup and running as good as possible, so that it can work for you too. The side-goals for this project is to learn new technologies that I have not yet worked. E.g. I introduced myself to ElasticSearch, my first usage of a NoSQL-database, and the Kotlin programming language, learn the Angular web framework for the GUI, use Gradle as build tool, but also JSoup and Spotify-web-api to name a few smaller frameworks that were new to me.

I have some additional ideas to add to the program, more in the area of functional requirements, like

1. Improve the algorithm that detects if a song is already in a playlist or not
1. Support more music charts, like Billboard etc.
1. Support more music providers, like Deezer, Apple music or Google play.
1. Improve the GUI

If you would like to help, send me a message. Let me know what you want to do and what information you need to get you started. And submit your changes for review via a pull request.

I hope you enjoy this project.

Kind regards,

Dave
