(ns myhackytests
  (:require [myserver :as m]          
  ))


(defn perfect_feature [item] (* 0.1 (.length item)))

(defn useless_feature [item] (* 0.1 (java.lang.Integer/parseInt (.substring item 0 1))))

(def features [perfect_feature useless_feature])

(def classifier {:one "LONG" 
                 :zero "SHORT"
                 :intercept 90.42694206220736
                 :weights [-510.0369285921699 -29.78327218110678]
                 :averages [0.15 0.35]
                 })

(println (m/classify "1" features classifier 0.1))
(println (m/classify "11" features classifier 0.1))
(println (m/classify "1111111111111111111111111111111" features classifier 0.1))
(println (m/classify "a" features classifier 0.4))
(println (m/classify "a" features classifier 0.6))
(println (m/classify "aa" features classifier 0.4))
(println (m/classify "aa" features classifier 0.6))

(def labeleditems '(["1" "SHORT"], ["11" "LONG"], 
                    ["2" "SHORT"], ["22" "LONG"], 
                    ["3" "SHORT"], ["33" "LONG"], 
                    ["4" "SHORT"], ["44" "LONG"], 
                    ["5" "SHORT"], ["55" "LONG"], 
                    ["6" "SHORT"], ["66" "LONG"], ["a" "SHORT"]))

(println (m/trainmatrix labeleditems features 0.1))
(println (m/trainmatrix labeleditems features 0.9))

(println (m/transpose(m/trainmatrix labeleditems features 0.1)))
(println (m/transpose(m/trainmatrix labeleditems features 0.9)))

(println (m/average '(1 9 11)))
(println (m/removenils '(1 nil nil 9 11 nil)))

(println (m/calcaverages(m/transpose(m/trainmatrix labeleditems features 0.1))))
(println (m/calcaverages(m/transpose(m/trainmatrix labeleditems features 0.9))))