(defproject logregdogtweetpuller "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.twitter4j/twitter4j-stream "4.0.2"]]
  
  :repositories {"twitter4j.org" "http://twitter4j.org/maven2"}
  
  :repl-options {:init-ns tweetpuller}
)
