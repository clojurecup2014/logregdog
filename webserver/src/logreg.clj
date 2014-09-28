(ns logreg 
    (:import 
            (weka.core Attribute FastVector Instance Instances)
  )
)


;; logistic regression classifier:

(defn evalfeat [feature item] 
  (let [evaluation (try (feature item) (catch java.lang.Throwable t nil))]
    (if evaluation 
      (if (< evaluation 0.0) 0.0  
        (if (> evaluation 1.0) 1.0 evaluation)
      )
      nil)
    )
  )

(defn evalallfeats [features item]
  (map #(evalfeat % item) features)
  )

(defn maxbadcheck [evals maxbad]
  (if (> 
        (/ (+ 0.0 (reduce + (map #(if % 0 1) evals))) (count evals)) 
        maxbad) nil evals)
  )

(defn replacenils [evals averages]
  (map #(if (nth % 0) (nth % 0) (nth % 1)) (map vector evals averages))
  )

(defn weightedsum [evals weights]
  (reduce + (map #(* (nth % 0) (nth % 1)) (map vector evals weights)))
  )

(defn calcres [weightedsum intercept]
  (- 1.0 (/ 1.0 (+ 1.0 (java.lang.Math/exp (* -1.0 (+ intercept weightedsum))))))
  )

(defn predict [res zero one]
  (if (< res 0.5) zero one)
  )

(defn classify [item features classifier maxbad]
  (let [evals (evalallfeats features item)]
    (if (maxbadcheck evals maxbad)
      (predict 
        (calcres (weightedsum (replacenils evals (:averages classifier)) 
                              (:weights classifier)) (:intercept classifier))
      (:zero classifier) (:one classifier))
      nil
      )
    )
  )

;; training of the logistic regression classifier:

(defn trainmatrix [labeled_items features maxbad]
   (let [matrix (map #(evalallfeats features %) (map #(nth % 0) labeled_items))]
     (filter #(maxbadcheck % maxbad) matrix)
     )
  )

(defn transpose [matrix] (apply map list matrix) )

(defn removenils [list]
  (filter (complement nil?) list)
  )

(defn average [list] 
    (/ (+ 0.0 (reduce + list)) (count list))
  )

(defn calcaverages [transposed] 
  (vector (map (comp average removenils) transposed))
  )

(defn cats [labeled_items] (list (set (map #(nth % 1) labeled_items))))

(defn catsAsFastVector [cats]
  (let [res (new FastVector)]
    (.addElement res (first cats))
    (.addElement res (last cats))
    res)
  )

(defn attributesAsFastVector [catsAsFastVector features]
  (let [res (new FastVector)]
    (.addElement res (new Attribute "label" catsAsFastVector 0))
    (doseq [i (range (count features))]
       (.addElement res (name (nth features i)) (+ i 1)))
    res)
  )

(defn nemtom [attributesAsFastVector matrix features labeled_items]
  (let [instances (new Instances "" attributesAsFastVector 0)]
       (doseq [i (range (count matrix))]
            (let [instance (new Instance (+ 1 (count features)))]
                  (.setDataset instance instances)
                  (.setValue instance (.elementAt attributesAsFastVector 0)
                        (last (nth labeled_items i)))
                  
                  
              )
       )
    )
  )