(ns tweetpuller
  (:import (twitter4j StatusListener TwitterStreamFactory))
)

(def listener (proxy [StatusListener] [] 
                (onDeletionNotice [arg0] ())
                (onScrubGeo [arg0 arg1] ())
                (onTrackLimitationNotice [arg0] ())
                (onStallWarning [arg0] ())
                (onException [arg0] ())
                (onStatus [arg0] (println (.getText arg0)))
                ))

(def spritzer (.getInstance (new TwitterStreamFactory)))

(.addListener spritzer listener)

(.sample spritzer)