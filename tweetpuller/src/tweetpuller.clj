(ns tweetpuller
  (:import (twitter4j StatusListener TwitterStreamFactory)
           (java.lang Thread System))
)

(def collected (atom ()))

(def listener (proxy [StatusListener] [] 
                (onDeletionNotice [arg0] ())
                (onScrubGeo [arg0 arg1] ())
                (onTrackLimitationNotice [arg0] ())
                (onStallWarning [arg0] (.sleep Thread 5000))
                (onException [arg0] (.printStackTrace arg0))
                (onStatus [arg0] 
                    (swap! collected conj (.replaceAll (.getText arg0) "\r|\n" " "))
                    (if (> (count (deref collected)) 100)  
                      (do 
                        (spit (str "../tweets/" (System/currentTimeMillis)) (clojure.string/join "\n" (deref collected)))
                        (reset! collected ())
                        )
                      )
                  )
                ))

(def spritzer (.getInstance (new TwitterStreamFactory)))

(.addListener spritzer listener)

(.sample spritzer)
