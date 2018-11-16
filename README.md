# playlists
## Purpose
Compile Spotify playlists based on music titles taken from music charts published on the internet.

## Background
Like most people, I love music. But the last couple of years I hardly listen to the radio anymore, so I was lacking 
input of new music being published. And like some people, I am kind of a nerd. So one day, I started to compile a playlist 
on Spotify per year of all music released in that year. I went over the charts at top40.nl (yes, I am Dutch) and for 
each week, for every new entry, I looked the song up on spotify, and added it to the playlist. When I completed an annum, 
I would listen to all the songs in the playlist and rate them. Each song I liked, I added to a playlist called "Nice", but when 
I thought a song was awesome, I added it to a playlist called "Good and better" instead. Those two are my main playlists 
for popmusic.

I timed my efforts to compile the playlist on spotify and I estimated that it took me about 4 hours per quarter, that 
is 16 hours per year. I have done this now for about a decade, and the nerd in me wants to be complete, so I have a 
couple of more decades to go. That amounts to a really large number of hours compiling playlists, that I can also spent 
in better ways.

Thus, one day, I thought that maybe I should automate the process. This little program is the result. 

## Implementation
top40.nl does not provide an API to get the weekly charts in an automated fashion. So this program reads the html pages 
from internet, extracts the relevant information from it and stores it in a database. Then it can compile a list of all 
releases in a certain year, create a Spotify playlist and fill it with as much of the songs it can find on Spotify.
Songs that could not be found are listed in the application log. You can try to manually find them yourself and add it to the playlist. Oftentimes you will succeed, because the artist / title from the music charts sometimes uses a different 
spelling than Spotify. But sometimes a song is not available, or a song is only available performed by a coverband.

When you instruct the program multiple times to compile the playlist for the same year, it will never remove songs from 
the existing playlist, it will only add new songs when they are not yet present.

## Setup
What you need to get this program running, is the following:
1. A running ElasticSearch database
1. A clientId that you receive when you register this application with Spotify
1. A Java IDE with Java 8 or newer

Below each requirement is discussed in more detail.

### A running ElasticSearch database
The application persists a collection of songs in an ElasticSearch database. I chose a NoSQL database over a relational
one, only to gain some experience with this type of database. Installing ElasticSearch is easy; you can find instructions
here: https://www.elastic.co/guide/en/elasticsearch/reference/current/_installation.html

Next you need to provide connection details to the playlists application via a property file called
`elasticsearch.properties`, which must be located in you home folder, in the subdirectory called `.playlists`. You 
need to create these mannually as part of the setup process. The property file must contain two key-value pairs, for the 
properties `es.hostname` and `es.portnumber`.

If you run elasticsearch on your localhost on port 9200 (the default), then the contents of elasticsearch.properties would look like this:

```
es.hostname=localhost
es.portnumber=9200
```

If your installation requires more complex connection details, you need to change the sourcecode at `info.rsdev.playlists.ioc.SpringDatalayerConfig`. Check the elasticsearch documentation for details.

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
  need this lateron.
- Below that, you see the text "Show client secret". Click on the text to reveal it. A client secret is the password that 
  goes with the clientid. Copy the client secret, you need it lateron.
- Next, you need to click on the button 'Edit settings' in the same window. It will open the settings of your application 
  as recorded by Spotify. In the settings, you need to add a redirectUrl. The URL is used by Spotify to deliver the
  accessToken to your application. Normally, the URL would point to your web application and the accessToken would be 
  processed automatically. But since playlists is a command line application, the accessToken needs to be provided to the 
  playlists application manually. Therefore, the value you provide for the redirectUrl should be a non-existing url, E.g. 
  https://playlists.for.me. Fill your (dummy) redirectUrl in at the appropriate place in the settings (don forget to click 
  'Add' (nor 'Save´ at the bottom).
- You are now done registering the application with Spotify. If you like you can logout.

Next, we need to pass on the information from Spotify to the playlists program via a properties file. Follow these steps:
- In your home folder, in the sub directory `.playlists`, create a new file called `spotify.properties`. It must 
  contain the property `spotify.clientId` and `spotify.clientSecret`, both with the correct values you copied a few
  steps back.
- The spotify.properties file must also contain the property `spotify.redirectUrl`, with the value of the redirectUrl
  that you filled out on Spotify a few steps back.
- The spotify.properties must also contain a property called `spotify.accessToken`. This is a session token and the 
  application needs a valid token in order to be able to maintain playlists on behalf of some user. In the section below, 
  titled 'running', I will explain how to obtain an accessToken. For now you can leave the value empty.

At this point, the spotify.properties file could look something like this:

```
spotify.clientId=e43b7a0a10934c74442fe65abc4d6ee6
spotify.clientSecret=d08e81fd0bfb9ed0a0007ffd0ff70067
spotify.redirectUrl=https://playlists.for.me
spotify.accessToken=
```

### A Java IDE
Finally, you need to make small adjustments to the Java source code, in order to instruct for which year you want to 
create or update a playlist. Currently you can do this only one year at a time. I assume you are a (Java) developer and 
you have an Java IDE installed on your machine, E.g. Eclipse, NetBeans or IntelliJ Community edition.

First you need to clone the playlist project from Github.com. You can do this from the commandline or from your IDE. The
clone URL is https://github.com/dschoorl/playlists.git. It is a maven project and your IDE probably supports this  
build tool out of the box. The project has a simple structure: it contains no sub modules. Open or import the project 
into your IDE.

Edit the source file, located at the following path in your git workspace: `src/main/java/info/rsdev/playlists/Playlists.java`. At the bottom of the file, in the method `start`, you can change the year for which you want to 
create or update a playlist on Spotify. Change the value to your needs, save the file and recompile the project.

Run the project as a Java application from your IDE and read the following section.

#### Running - obtain a (temporary) Spotify accessToken
When you run the application, it needs a valid accessToken to access Spotify. Unfortunately, this process can not be done 
automatically, since playlists is a command line application. The token is obtained by the user manually. A token is 
usually valid for one hour. After that, you must repeat the authorization proces.

When there is no accessToken, or the token has expired, the application log will show a stacktrace containing a URL 
that you must copy/paste into your web browser. The URL looks something like this:

```
https://accounts.spotify.com/authorize?response_type=token&client_id=e43b7a0a10934c74442fe65abc4d6ee6&redirect_uri=https%3A%2F%2Fplaylists.for.me&scope=playlist-read-private%20playlist-modify-private%20playlist-modify
```

When you copy/paste the URL in the address bar of your browser, you are redirected to a Spotify authorization page where 
you, as a Spotify user, can provide your credentials. At the end of the process, your browser should show a standard 
error page indicating that the website pointed to cannot be found (it is the redirectUrl that you configured in Spotify 
dashboard). The url in your browser address bar now contains the accessToken that you need to fill out in the 
spotify.properties file.

if the url in your browser looks like this:

```
https://playlists.for.me/#access_token=BQBRKW4RPjjG1jYmuSRzT2-XwnDg8KOBmqw5GyWNSajjGE-oOL8Mj2T21agl7VnRIXRkUHmKkZc-lUWKAAcgxL-YHOKkcuN4RJBDcNQiIVijXCoCM4weB3Qwu_hW_yOifOzqsbe0SZ6rqlO6gxle_jKJtN75XgaLpfvY7CAAOE3JEhNarVHwDgGLNb4THssC6ipPLAy81w3JfiiLfaFaFOFXJykL21qP62bV&token_type=Bearer&expires_in=3600
```

Your spotify.properties should look like this after pasting the accessToken value:

```
spotify.clientId=e43b7a0a10934c74442fe65abc4d6ee6
spotify.clientSecret=d08e81fd0bfb9ed0a0007ffd0ff70067
spotify.redirectUrl=https://playlists.for.me
spotify.accessToken=BQBRKW4RPjjG1jYmuSRzT2-XwnDg8KOBmqw5GyWNSajjGE-oOL8Mj2T21agl7VnRIXRkUHmKkZc-lUWKAAcgxL-YHOKkcuN4RJBDcNQiIVijXCoCM4weB3Qwu_hW_yOifOzqsbe0SZ6rqlO6gxle_jKJtN75XgaLpfvY7CAAOE3JEhNarVHwDgGLNb4THssC6ipPLAy81w3JfiiLfaFaFOFXJykL21qP62bV
```

After replacing the previous spotify.accessToken value in the spotify.property file, you can re-run the application 
and it should now be able to connect to Spotify on your behalf.

First thing that the application will do, is go to the website of top40, load all the top-40 and tipparade info from
it, and store it in it's database. For the first time, this can take a while, since these charts date back to the sixties. In subsequent runs, it will only load missing editions of the supported music charts.

Then it will query the database to retrieve all released singles that entered these charts for the year you configured in 
the Class `Playlists.java`. It will log some interesting details and terminate when it's finished.

If you now open your spotify client, you can see a new (or updated) playlist with the name '<year> charted songs'. If 
you configured the Playlists.java class that this should be 2018, then the playlist is titled '2018 charted songs'.

You need to follow this procedure again for each year you want to create a playlist for.

## Status
The program works for me, because I know how to handle and run it. I tried to explain setup and running as good as 
possible, so it can work for you too. It took me 65 hours to write the first version that was feature complete for my 
purposes, so I have a return on investment of 4 years. I consider that to be pretty good. Meanwhile, I learned a few 
new things. I introduced myself to ElasticSearch, my first usage of a NoSQL-database, but also JSoup and 
Spotify-web-api to name a few smaller frameworks that were new to me.

I have some ideas to add to the program, like
1. provide startup parameters instead of re-program hardcodes values in the source files
1. Improve the algorithm that detects if a song is already in a playlist or not
1. Support more music charts, like Billboard etc.
1. Support more music providers, like Deezer, Apple music or Google play.
1. Provide GUI to visually compile playlists from custom queries on the elastic search database

But I probably do not have the time to implement them, due to other plans and commitments. However, I am open to 
receive pull requests with your stuff that makes the application more usefull for other people.

I hope you enjoy this project.

Kind regards,

Dave
