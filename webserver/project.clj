(defproject logregdogwebserver "0.1.0-SNAPSHOT"

  :dependencies [
                 ; clojure deps:
                 [org.clojure/clojure "1.6.0"]
                 [lib-noir "0.8.8"] ; includes compojure and ring
                 [ring-server "0.3.1"] ; an additional ring lib that allows starting the server from the repl
                 [com.taoensso/timbre "3.2.1"] ; for logging

                 ; clojurescript deps:
                 [org.clojure/clojurescript "0.0-2280"]
                 [reagent "0.4.2"] ; interface to react.js
                 [cljs-ajax "0.2.6"] ; for ajax calls
                ]

  ; myserver is our clojure namespace
  :repl-options {:init-ns myserver}

  ; configuration of clojurescript compilation:
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild
  {:builds
    [{:source-paths ["src-cljs"]
      :compiler
        {:optimizations :none
         :output-to "resources/public/js/app.js"
         :output-dir "resources/public/js/"
         :pretty-print true
         :source-map true
        }}]}

)

