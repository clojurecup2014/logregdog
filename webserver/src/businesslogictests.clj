(ns businesslogictests
      (:require [businesslogic :as b]          
  ))

(println  (b/filtered-tweets #(.contains % "the") true 5))

(def labeleditems '(("1" 1), ("11" 2), 
                    ("2" 1), ("22" 2), 
                    ("3" 1), ("33" 2), 
                    ("4" 1), ("44" 2), 
                    ("5" 1), ("55" 2), 
                    ("6" 1), ("66" 2), 
                    ("a" 1), ("aa" 0)))

(println (b/removezeroes labeleditems))

(defn perfect_feature [item] (* 0.1 (.length item)))

(defn useless_feature [item] (* 0.1 (java.lang.Integer/parseInt (.substring item 0 1))))

(def features [perfect_feature useless_feature])

(def classifier (b/train-classifier features labeleditems))

(println classifier)

