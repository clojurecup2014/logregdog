(ns businesslogic)

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



