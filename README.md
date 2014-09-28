DEPENDENCIES:

Fedora 20 server + git, screen, vim, iptables-services
nginx (as reverse proxy)
Clojure and ClojureScript, Oracle JDK 1.7, Google Closure Compiler, Leiningen
github.com/yogthos/reagent-example (as template for the web server)

http://bootswatch.com/simplex/bootstrap.min.css (might change)
http://jquery.com/ 
http://getbootstrap.com/

lib-noir and its dependencies (compojure, ring, jetty, etc.)
com.taoensso/timbre
reagent and react.js
cljs-ajax

the Twitter API, namely the free "spritzer"
twitter4j

Weka (just the jar file, used as library)


DONE SO FAR:

Configured server. Chose Fedora 20. Updated it and installed some essentials to it.

To log in: ssh clojurecup@logregdog.clojurecup.com (decided to use clojurecup user for everything since it needed to be created and to have access to the stuff anyway)

ssh works with key. I disabled ssh with password for clarity.

The important stuff is in /home/clojurecup/

To become root you need to do "sudo su": root login is disabled by the hosting service in Debian style.

Installed as service nginx as reverse proxy port 80 -> port 3000 (That was pain because of Fedora 20's SELinux blocking it)

Configured iptables to open port 80 but all else stay closed (Again pain in Fedora 20 as there is some mess with firewalld)

Installed Oracle JDK and Leiningen.

Made a copy of github.com/yogthos/reagent-example to use as template.

It is impossible to ssh from the server to other places (port 22 outbound connections blocked by provider), so I am pulling from github via https. That requires to enter github username and password every time, but that is not so bad.

When the clojure website is running, it now nicely appears at http://logregdog.clojurecup.com

I did a lot of simplifications to github.com/yogthos/reagent-example, removing libraries we will not be using, but not changing the overall functionality.

I finished the tweetpuller and tweetdeleter. To run them, one needs to create folder "tweets" in the main project folder manually.

The tweetpuller and tweetdeleter were left to run on the server for hours and seem to perform very fine.

Implemented the logistic regression classifier, including some small tests for it with a dummy example.

The logic for logistic regression classifier training is work in progress.

DECISIONS:

No state in the web server, all state is in the web client.

All state in the web client is in one map in one atom.

Please let's stick to as few namespaces as possible, as otherwise I get confused. We are not aiming for code beauty here, not making a nicely structured lib.

Please do not make this repository public because my twitter API credentials are in it.

COMPONENTS:

webserver: clojure and clojurescript

tweetpuller: clojure

tweetdeleter: bash

WEBSERVER:

To deploy it on the server: lein cljsbuild once; lein repl

We are deploying using lein repl (inside a screen).

For the webserver to build, the dependency on weka has to be installed manually. Copy the "weka" folder in the root of the project (yes, the one that is empty except for another "weka" folder in it) to ~/.m2/repository/

For example this works (when one is in the root of the project): cp -R weka ~/.m2/repository/

This manual installation of weka dependency has been done on the server.

Just for reference, the weka folder was created by running: mvn install:install-file -Dfile=weka-3.6.11.jar -DartifactId=weka -Dversion=3.6.11 -DgroupId=weka -Dpackaging=jar -DcreateChecksum=true

TWEETPULLER:

Again, lein repl in a screen is the way to start it.

To run it, one needs to create folder "tweets" in the main project folder manually.

Do not start it, because it is running on the server, and if it is running in several instances Twitter might block my API access!!!

TWEETDELETER:

Just an endless loop bashscript. Running it in a screen too.

To run it, one needs to create folder "tweets" in the main project folder manually.

Has to be launched from the folder where it is.

We will be keeping at any point of time 1000 files of 100 tweets each.
