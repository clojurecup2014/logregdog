(ns logreg 
    (:import 
            (weka.core Attribute FastVector Instance Instances)
            (weka.classifiers.functions Logistic)
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

(defn getnumev [evals averages weights intercept]
  (calcres (weightedsum (replacenils evals averages) 
                              weights) intercept)
  )

(defn classify [item features classifier maxbad]
  (let [evals (evalallfeats features item)]
    (if (maxbadcheck evals maxbad)
      (predict 
        (getnumev evals (:averages classifier) (:weights classifier) (:intercept classifier))
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

(defn cats [labeled_items] (set (map #(nth % 1) labeled_items)))

(defn catsAsFastVector [cats]
  (let [res (new FastVector)]
    (.addElement res (first cats))
    (.addElement res (first (rest cats)))
    res)
  )

(defn attributesAsFastVector [catsAsFastVector features]
  (let [res (new FastVector)]
    (.addElement res (new Attribute "label" catsAsFastVector 0))
    (doseq [i (range (count features))]
       (.addElement res (new Attribute
         (str (+ i 1))
         ;; (name (quote (nth features i))) 
         (+ i 1))))
    res)
  )

(defn createinstances [attributesAsFastVector matrix features labeled_items]
  (let [instances (new Instances "" attributesAsFastVector 0)]
       (doseq [i (range (count matrix))]
            (let [instance (new Instance (+ 1 (count features)))]
                  (.setDataset instance instances)
                  (.setValue instance (.elementAt attributesAsFastVector 0)
                        (last (nth labeled_items i)))
                  (let [itemdata (nth matrix i)]
                           (doseq [j (range (count itemdata))]
                                  (let [itemdatapiece (nth itemdata j)]
                                          (if itemdatapiece
                                                 (.setValue instance
                                                   (.elementAt attributesAsFastVector (+ 1 j))
                                                   itemdatapiece
                                                   )
                                            )
                                    )
                            )
                    )
              (.add instances instance))
       )
    (.setClassIndex instances 0)
    instances)
  )

(defn getclassifier [instances] 
     (let [logistic (new Logistic)]
            (.setRidge logistic 1.0E-8)
            (.setMaxIts logistic -1)
            (.buildClassifier logistic instances)
            logistic
       )
  )

(defn get_coefs [labeled_items features maxbad]
  (.coefficients (getclassifier 
    (createinstances 
            (attributesAsFastVector (catsAsFastVector (cats labeled_items)) features)
            (trainmatrix labeled_items features maxbad)
            features
            labeled_items
      )                         
  ))
  )

(defn getintercept [coefs] (first (first coefs)))

(defn getweights [coefs] (mapv  first (rest coefs)))

(defn evtolabel [matrix averages weights intercept labeled_items]
    (map vector (map #(getnumev % averages weights intercept) matrix) (map second labeled_items))
  )

(defn evsofcat [cat evtolabel]
  (map first (filter #(.equals cat (first (rest %))) evtolabel))
   )

(defn avtocat [cats evtolabel]
     (map #(vector % (average (evsofcat % evtolabel))) cats)
  )

(defn zeroandone [avtocat]
  (if 
    (< (first (rest (first avtocat))) (first (rest (first (rest avtocat)))))
    [(first (first avtocat)) (first (first (rest avtocat)))]
    [(first (first (rest avtocat))) (first (first avtocat))]
    )
  )

(defn clasconf [labeled_items features maxbad]
 (let [coefs (get_coefs labeled_items features maxbad)]
      (let [matrix (trainmatrix labeled_items features maxbad)]
     (let  [averages (calcaverages(transpose matrix))]
         (let  [weights (getweights coefs)]
        (let   [intercept (getintercept coefs)]
           (let  [evtolabels (evtolabel matrix averages weights intercept labeled_items)]
                (let [zando (zeroandone(avtocat (cats labeled_items) evtolabels))]
                     {:one (first (rest zando)) 
                      :zero (first zando) 
                      :intercept intercept
                      :weights weights
                      :averages averages
                      }
                 )
           ))))
       )
  )
)

; TODO 1: calc precision
; TODO 2: handle constant feature
; TODO 3: do not calc cats twice
