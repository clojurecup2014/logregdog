DEPENDENCIES:

Fedora 20 server + git, screen, vim, iptables-services

nginx (as reverse proxy)

Clojure and ClojureScript, Oracle JDK 1.7, Google Closure Compiler, Leiningen

github.com/yogthos/reagent-example (as template for the web server)

http://bootswatch.com/simplex/bootstrap.min.css (might change)

react.js

lib-noir and its dependencies (compojure, ring, jetty, etc.)

com.taoensso/timbre

reagent

cljs-ajax


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

DECISIONS:

No state in the web server, all state is in the web client.

All state in the web client is in one map in one atom.

COMPONENTS:

webserver: clojure and clojurescript

tweetpuller: clojure

tweetdeleter: bash only, I guess

WEBSERVER:

To deploy it on the server: lein cljsbuild once; lein repl

We will be deploying using lein repl (inside a screen) not only because I like it, but also because people who are making uberjars have run into an interesting limitation from the server provider: max 100 builds.