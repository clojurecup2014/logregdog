(ns businesslogic
    (:require [logreg :as l]          
  ))

(defn filtered-tweets [filter-fun delayed max]
    (let [files (reverse (sort (map #(java.lang.Long/parseLong (.getName %)) 
                                    (.listFiles (new java.io.File "../tweets/")))))]
         (let [partitions (partition (int (/ (count files) 2 )) files)]
                (let [good (if delayed  (first (rest partitions))  (first partitions)  )]
                       (let [tweets (flatten (map 
                                     #(try (clojure.string/split (slurp (str "../tweets/" %)) #"\n") 
                                        (catch java.lang.Throwable t '()))
                                                  good))]
                         
                         (take max (lazy-seq (filter filter-fun tweets)))
                         
                       )
                  )
          )
    )
)

(def maxbad 0.55) ; because why not :)

(defn removezeroes [labeled-tweets]
       (filter #( > (first (rest %)) 0 ) labeled-tweets)
       )

(defn train-classifier [list-of-feature-funs labeled-tweets]
  (let [classifier
  
  (l/clasconf 
    (map #(list (first %)  (str (first (rest %)))   )  (removezeroes labeled-tweets)) 
    list-of-feature-funs maxbad  )
  ]
     
        (conj 
          
              (conj classifier [:zero 
                      (java.lang.Integer/parseInt (:zero classifier))
                      ])
              
              [:one 
                      (java.lang.Integer/parseInt (:one classifier))
                      ])
    
    )
  )
