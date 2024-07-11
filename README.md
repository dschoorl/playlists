# playlists

> [!IMPORTANT]  
> I started development of a Web GUI with the Angular framework to improve the user experience. This change makes part of the documentation below outdated. This will be fixed when a first version of the Web GUI is finalized.

## Purpose
Compile Spotify playlists from music charts published on the internet.

## Background
Like most people, I love music. But the last couple of years I hardly listen to the radio anymore, so I was lacking 
input of new music being published. And like some people, I am kind of a nerd. So one day, I started to compile a playlist 
on Spotify per year of all music released in that year. I went over the charts at top40.nl (yes, I am Dutch) and for 
each week, for every new entry, I looked the song up on spotify, and added it to the playlist. When I completed an annum, 
I would listen to all the songs in the playlist and rate them. Each song I liked, I added to my playlist called "Nice", and when 
I thought a song was awesome, I added it to a playlist called "Good and better" instead. Those two are my main playlists 
for listening to mainstream pop music.

I timed my efforts to compile the playlist on spotify and I estimated that it took me about 4 hours per quarter, that 
is 16 hours per year. I have done this now for ten volumes of the top40, and the nerd in me wants to be complete, so I 
have a couple of more volumes to go. That amounts to a really large number of hours compiling playlists, that I can also 
spent in better ways.

Thus, one day, I thought that maybe I should automate the process. This little program is the result. 

## Implementation
top40.nl does not provide an API to get the weekly charts in an automated fashion. So this program reads the html pages 
from internet, extracts the relevant information from it and stores it in a database. Then it can compile a list of all 
releases in a certain year, create a Spotify playlist and fill it with as much of the songs it can find on Spotify.
Songs that could not be found are listed in the application log. You can try to manually find them yourself and add it to the playlist. Oftentimes you will succeed, because the artist / title from the music charts sometimes uses a different 
spelling than Spotify. But sometimes a song is just not available, or a song is only available performed by a coverband.

When you instruct the program multiple times to compile the playlist for the same year, it will never remove songs from 
the existing playlist, it will only add new songs when they are not already present.

## Setup
What you need to get this program running, is the following:
1. A running MariaDb database
1. A clientId that you receive when you register this application with Spotify
1. A Java 21 SDK (or newer)

Below each requirement is discussed in more detail.

### A running MariaDb database
When I started the project, I choose a NoSQL database over a relational one, only for me to gain some experience with this type 
of database. However, it is not a good fit for this application, so I decided to change it back to a relational one. Since September 2023 
it will use a MariaDb relational database.

If you not already have a MariaDb server running (MySql should work as well), please install one. You can find installation instructions 
for windows [here](https://www.mariadbtutorial.com/getting-started/install-mariadb/) or for Linux 
[here](https://www.digitalocean.com/community/tutorial-collections/how-to-install-mariadb). I have tested the application against MariaDb 
10.11. The application uses Liquibase to update the table structure over time. You must only create the database yourself plus create two 
accounts, one with rights to maintain the data and one with the rights to maintain the datbase structure. You could use the sql below for 
inspiration:

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

You must substitute these dummy passwords with the values you have used when you created the accounts.


### A Spotify clientId
Before the playlists application can access Spotify, you must register it with Spotify and receive a clientId. A 
clientId is personal information, and you should not share it with others. Completing the registration process is 
(almost) a breeze:

- Go to the Spotify developers website's dashboard at https://developer.spotify.com/dashboard/
- Click on login and use an existing Spotify account or create a new developer account
- From the dashboard, you can click the 'create clientid' button or click on the 'my new app' tile. Both do the same thing.
- Fill out the paperwork. Choose the name you like and also select desktop app as type of application and I guess 
  choosing non commercial integration would make sense.
- When finished, you are taken to the details of the newly created Spotify app. There you see the ClientId. Copy it, you 
  need this later on.
- Below that, you see the text "Show client secret". Click on the text to reveal it. A client secret is the password that 
  goes with the clientid. Copy the client secret, you need it lateron.
- Next, you need to click on the button 'Edit settings' in the same window. It will open the settings of your application 
  as recorded by Spotify. In the settings, you need to add a redirectUrl. The URL is used by Spotify to deliver the
  authorization code to your application. Normally, the URL would point to a callback function in your web application and 
  the authorization code would be processed automatically. But since playlists is a command line application, the 
  authorization code needs to be provided to the 
  playlists application via a properties file. Therefore, the value you provide for the redirectUrl should be a non-existing url, E.g. 
  https://playlists.for.me. Fill your (dummy) redirectUrl in at the appropriate place in the settings (don forget to click 
  'Add' (nor 'Save´ at the bottom).
- You are now done registering the application with Spotify. If you like you can logout.

Next, we need to pass on the information from Spotify to the playlists program via a properties file. Follow these steps:
- In your home folder, in the sub directory `.playlists`, create a new file called `spotify.properties`. It must 
  contain the property `spotify.clientId` and `spotify.clientSecret`, both with the correct values you copied a few
  steps back.
- The spotify.properties file must also contain the property `spotify.redirectUrl`, with the value of the redirectUrl
  that you filled out on Spotify a few steps back.
- The spotify.properties must also contain a property called `spotify.authCode`. This is the authorization code and the 
  application needs a valid code in order to be able to maintain playlists on behalf of some user. In the section below, 
  titled 'running', I will explain how to obtain an authorization code. For now you can leave the value empty.

At this point, the spotify.properties file could look something like this:

```
spotify.clientId=e43b7a0a10934c74442fe65abc4d6ee6
spotify.clientSecret=d08e81fd0bfb9ed0a0007ffd0ff70067
spotify.redirectUrl=https://playlists.for.me
spotify.authCode=
```

### A Java 21 SDK (or newer)
The application runs on a Java 21 Virtual Machine, so you must have a working installation on your computer. The project 
uses Gradle as build tool. You do not need to have Gradle installed prior to building the project, because it will download
itself automatically. However, you do need git installed on your machine.

First you need to clone the playlist project from Github.com to your local machine. You can do this from the commandline 
or from your favourite IDE (if your are a software developer)). The clone URL is https://github.com/dschoorl/playlists.git.

From the commandline, go into the 'playlist' subdirectory that was created by the git-clone command. This is the project
directory. From here you can build and run the application.

Execute the following command to build the project:   
On Windows: ```gradelw.bat build ```   
On Mac/Linux: ```./gradlew build```

This will create a fat jar in the sub directory called `playlist-server/build/libs` that can be executed with the `java -jar` command:  

On Windows: `java -jar playlist-server\build\libs\playlists.jar --spring.profiles.active=local`   
On Mac/Linux: `java -jar playlist-verver/build/libs/playlists.jar --spring.profiles.active=local`   

Please continue reading the next section where it is explained what the application is doing or trying to do.

#### Running - obtain a (temporary) Spotify authorization code

When you run the application, it needs a valid authorization code to access Spotify. Unfortunately, this process can not be done 
automatically, since playlists is a command line application. The token is obtained by the user manually. A code is 
usually valid for one run. After that, you must repeat the authorization proces.

When there is no authorization code, or the token has expired, the application log will show a stacktrace containing a URL 
that you must copy/paste into your web browser. The URL looks something like this:

```
https://accounts.spotify.com/authorize?response_type=code&client_id=e43b7a0a10934c74442fe65abc4d6ee6&redirect_uri=https%3A%2F%2Fplaylists.for.me&scope=playlist-read-private%20playlist-modify-private%20playlist-modify%20user-read-private%20user-read-email
```

When you copy/paste the URL in the address bar of your browser, you are redirected to a Spotify authorization page where 
you, as a Spotify user, can provide your credentials. At the end of the process, your browser should show a standard 
error page indicating that the website pointed to cannot be found (it is the redirectUrl that you configured in Spotify 
dashboard). The url in your browser address bar now contains the accessToken that you need to fill out in the 
spotify.properties file.

if the url in your browser looks like this:

```
https://playlists.for.me/?code=AQDqz-uel9A0RatFTpHxh0byHzOKtCnj-4aPzFwjNZruUQht7__lTZuqXXxEJQarcnGnx6RRDlDtJsWzdW32msi8FmBAzaEApqm5U_viwngKEbliZa_0BF9t93QBmC5jEJVeO7jhpDYp4mE2_lJn7OH2rx26OTk7p-6SITTNKpXqmgXKazJveuCC9ZRXiUH1XfLlTJWJyZneqWT0yA47uWl3PJFLxcEdXAYr2gDJFK-P7AFukpZBdv7Uje-sqxnXStr46Hbc7XUNymoC7RZlpSzVArrP9UDrWxGe#_=_
```

Your spotify.properties should look like this after pasting the authorization code (if the code ends with `#_=_`, leave those characters off):

```
spotify.clientId=e43b7a0a10934c74442fe65abc4d6ee6
spotify.clientSecret=d08e81fd0bfb9ed0a0007ffd0ff70067
spotify.redirectUrl=https://playlists.for.me
spotify.authCode=AQDqz-uel9A0RatFTpHxh0byHzOKtCnj-4aPzFwjNZruUQht7__lTZuqXXxEJQarcnGnx6RRDlDtJsWzdW32msi8FmBAzaEApqm5U_viwngKEbliZa_0BF9t93QBmC5jEJVeO7jhpDYp4mE2_lJn7OH2rx26OTk7p-6SITTNKpXqmgXKazJveuCC9ZRXiUH1XfLlTJWJyZneqWT0yA47uWl3PJFLxcEdXAYr2gDJFK-P7AFukpZBdv7Uje-sqxnXStr46Hbc7XUNymoC7RZlpSzVArrP9UDrWxGe
```

After replacing the previous spotify.authCode value in the spotify.property file, you can re-run the application 
and it should now be able to connect to Spotify on your behalf.

First thing that the application will do, is go to the website of top40, load all the top-40 and tipparade info from
it, and store it in it's database. For the first time, this can take a while, since these charts date back to the sixties. 
In subsequent runs, it will only load missing editions of the supported music charts.

Then it will query the database to retrieve all released singles that entered these charts for the year you asked for
through a commandline argument. It will log some interesting details like displayed below:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.0.RELEASE)

2018-11-21 09:53:51.471  INFO 8913 --- [           main] i.rsdev.playlists.Playlists$Companion    : Starting Playlists.Companion
2018-11-21 09:53:51.474  INFO 8913 --- [           main] i.rsdev.playlists.Playlists$Companion    : No active profile set, falling back to default profiles: default
2018-11-21 09:53:53.043  INFO 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Read 745 cache entries from file
2018-11-21 09:53:53.202  INFO 8913 --- [           main] i.rsdev.playlists.Playlists$Companion    : Started Playlists.Companion in 2.038 seconds (JVM running for 2.482)
2018-11-21 09:53:53.203  INFO 8913 --- [           main] info.rsdev.playlists.Playlists           : Program arguments: [2018]
2018-11-21 09:53:53.427  WARN 8913 --- [           main] i.r.p.services.MusicChartsService        : Datastore contains data for TOP40 from 2018, week 46
2018-11-21 09:53:53.438  WARN 8913 --- [           main] i.r.p.services.MusicChartsService        : Datastore contains data for TIPPARADE from 2018, week 46
2018-11-21 09:53:54.362  INFO 8913 --- [           main] i.r.p.services.Top40ScrapeService        : Scraped week 46 of Top 40 2018
2018-11-21 09:53:55.510  INFO 8913 --- [           main] i.r.p.services.Top40ScrapeService        : Scraped week 46 of Tipparade 2018
2018-11-21 09:53:55.573  INFO 8913 --- [           main] info.rsdev.playlists.Playlists           : Datastore initialized after 2s
2018-11-21 09:53:55.709  INFO 8913 --- [           main] i.r.playlists.services.PlaylistService   : Searching for 310 titles in playlist 2018 charted songs
2018-11-21 09:53:56.900 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Drake, title=God's plan) with q='drake track:god's plan'
2018-11-21 09:54:00.508 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Selena Gomez, title=Back 2 you '18) with q='gomez selena track:'18 2 back you'
2018-11-21 09:54:01.560 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Snow Patrol, title=Don't give in) with q='patrol snow track:don't give in'
2018-11-21 09:54:01.998 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Arlissa and Jonas Blue, title=Hearts ain't gonna lie) with q='arlissa blue jonas track:ain't gonna hearts lie'
2018-11-21 09:54:02.979 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Marco Borsato & André Hazes & Nick & Simon & Jeroen Van Koningsbrugge & Diggy Dex & Xander De Buisonjé & VanVelzen, title=Vrienden - Themasong De Vrienden Van Amstel Live!) with q='andré borsato buisonjé de dex diggy hazes jeroen koningsbrugge marco nick simon van vanvelzen xander track:amstel live! themasong vrienden'
2018-11-21 09:54:03.852 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=SFB/Ronnie Flex, title=One time) with q='flex sfb/ronnie track:one time'
2018-11-21 09:54:04.129 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Lucas & Steve x Janieck, title=You don't have to like it) with q='janieck lucas steve track:don't have it like to you'
2018-11-21 09:54:04.979 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Labrinth & Sia & Diplo present... LSD, title=Thunderclouds) with q='diplo labrinth lsd present... sia track:thunderclouds'
2018-11-21 09:54:05.027 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=CMC$ & Grx feat. Icona Pop, title=X's) with q='cmc$ grx icona pop track:x's'
2018-11-21 09:54:05.092 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Guwop x Mars x Kodak, title=Wake up in the sky) with q='guwop kodak mars track:in sky up wake'
2018-11-21 09:54:06.478 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=P.T.M., title=Live in the moment) with q='p.t.m. track:in live moment'
2018-11-21 09:54:06.873 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=SFB & Murda & Spanker, title=Shutdown) with q='murda sfb spanker track:shutdown'
2018-11-21 09:54:07.585 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=XXX Tentacion x Lil Pump feat Maluma & Swae Lee, title=Arms Around You) with q='lee lil maluma pump swae tentacion xxx track:arms around you'
2018-11-21 09:54:07.773 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Juice Wrld, title=Lucid dreams (forget me)) with q='juice wrld track:dreams forget lucid me'
2018-11-21 09:54:07.823 DEBUG 8913 --- [           main] i.r.p.services.SpotifyCatalogService     : Not found on Spotify: Song(artist=Ali B, R3hab, Numidia & Cheb Raya, title=Dana) with q='ali b cheb numidia r3hab raya track:dana'
2018-11-21 09:54:08.229  WARN 8913 --- [           main] i.r.playlists.services.PlaylistService   : Found 295 / of 310 on spotify
2018-11-21 09:54:08.600  INFO 8913 --- [           main] i.r.playlists.services.PlaylistService   : Added 295 songs to playlist 2018 charted songs
2018-11-21 09:54:08.600  INFO 8913 --- [           main] info.rsdev.playlists.Playlists           : Finished: 15s

```

The program keeps running, although it's doing nothing and you need to stop it with Ctrl-C. 

If you now open your spotify client, you can see a new (or updated) playlist with the name '\<year> charted songs'. If 
you asked for 2018, then the playlist is titled '2018 charted songs'.

You need to follow this procedure again for each year you want to create a playlist for. 

## Status
The program works for me, because I know how to handle and run it. I tried to explain setup and running as good as 
possible, so that it can work for you too. It took me 65 hours to write the first version that was feature complete for my 
purposes, which gives me a return on investment of 4 years. I consider that to be pretty good. Meanwhile, I learned a few 
new things. I introduced myself to ElasticSearch, my first usage of a NoSQL-database and Kotlin, but also JSoup and 
Spotify-web-api to name a few smaller frameworks that were new to me.

I will continue to support this project. My original goal was to use this project as a playground for new technologies, but recently I am trying to find a balance between productivity and experimenting with new technologies. As a result I switched back from Kotlin to Java 17 and from ElasticSearch to MariaDb. 

I have some additional ideas to add to the program, more in the area of functional requirements, like
1. Improve the algorithm that detects if a song is already in a playlist or not
1. Support more music charts, like Billboard etc.
1. Support more music providers, like Deezer, Apple music or Google play.
1. Provide GUI to visually compile playlists from custom queries on the elastic search database

If you would like to help, send me a message. Let me know what you want to do and what information you need to get you 
started. And submit your changes for review via a pull request.

I hope you enjoy this project.

Kind regards,

Dave
