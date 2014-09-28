(ns myclient
  (:require [reagent.core         :as reagent]
            [ajax.core            :as ajax]
            [clojure.browser.repl :as repl]
  ))

;; listen to the ClojureScript REPL
(repl/connect "http://localhost:9000/repl")

(enable-console-print!)

(def state
  (reagent/atom
   {:filter "; a tweet is just a string

(defn condition [tweet] true)"
    :filter-applied? false
    :features "; functions should return value between 0 and 1

(defn feature_length [tweet]
  (/ (count tweet) 140))

(defn has_a [tweet]
  (if (.contains tweet \"a\") 1 0))

; Please use defn only!
; For each feature, 
; it completely does not matter
; for what it goes towards 0 
; and for what towards 1:
; training will take care of that."
    :trained? false
    :features-enabled false
    }))

(def help
  {:filter [:span [:strong "HELLO!"] " I am the logregdog, your canine guide
to logistic regression (a form of machine learning). I am
just a dog, so there will be NO MATH! Start by pushing the
button above to get some fresh tweets. If you want to narrow
down what type of tweets you want to get, edit the filter
function above. For example a dog simple way to get English
tweets only is to check if they contain the :) Smart, eh?"]
   
   :features "Logistic regression is all about splitting things
into two groups, when it is hard to do, and you need many criteria
to do it right. For example, lets say you want to separate tweets 
into happy and sad. I would have one feature checking for happy
smileys, one checking for sad smileys, one for words like \"bone\"
and \"walk\", one for words like \"alone\" and \"cat\". And one checking
how long the tweet is, because people unhappy with something can be
quite verbose!
"
   })

(defn set-val! [id value]
  (swap! state assoc id value))

(defn row [label & body]
  [:div.row
   [:div.col-md-2 [:span label]]
   [:div.col-md-3 body]])

(defn text-input [id label]
  [row label
   [:input {:type "text"
            :class "form-control"
            :value (get-value id)
            :on-change #(set-value! id (-> % .-target .-value))}]])

(defn list-item [id k v selections]
  (letfn [(handle-click! []
            (swap! selections update-in [k] not)                         
            (set-value! id (->> @selections (filter second) (map first))))]
    [:li {:class (str "list-group-item" (if (k @selections) " active"))
          :on-click handle-click!}
      v]))

(defn selection-list [id label & items]
  (let [selections (->> items
                        (map (fn [[k]] [k false]))
                        (into {})
                        reagent/atom)]    
    (fn []
      [:div.row
       [:div.col-md-2 [:span label]]
       [:div.col-md-5
        [:div.row
         (for [[k v] items]
          [list-item id k v selections])]]])))

(defn update-field [id id-old pre value]
  ;;(prn id (id-old @state) value)
  (set-val! pre (= value (id-old @state)))
  (set-val! id value))

(def update-filter
  (partial update-field :filter :filter-old :filter-applied?))

(def update-features
  (partial update-field :features :features-old :trained?))

(defn set-labels! [tweets]
  (let [labels (zipmap (iterate inc 0)
                       (take (count tweets) (repeat 0)))]
    (set-val! :labels labels)))

(defn enough-labels? []
  (let [l (map second (:labels @state))
        l (distinct (filter (complement zero?) l))]
    (prn l)
    (= 2 (count l))))

(defn apply-filter []
  (let [ffun (:filter @state)]
    (ajax/POST (str js/context "/filtered-tweets")
               {:params {:filter-fun ffun
                         :delayed true
                         :max 20}
                :handler (fn [r]
                           ;; (prn r)
                           (set-val! :trained? false)
                           (set-val! :filter-applied? true)
                           (set-val! :filter-old ffun)
                           (set-val! :tweets r)
                           (set-labels! r)
                           (set-val! :features-enabled true))})))

(defn test-config []
  (let [s @state]
    (ajax/POST (str js/context "/test")
               {:params {:features (:features s)
                         :filter-fun (:filter s)
                         :max 30
                         :config (:config s)}
                :handler (fn [r]
                           ;; (prn r)
                           (set-val! :tested r))})))
(defn train []
  (let [s @state]
    (ajax/POST (str js/context "/train")
               {:params {:features (:features s)
                         :labels (map (fn [[k v] t]
                                        (list t v))
                                      (:labels s)
                                      (:tweets s))}
                :handler (fn [r]
                           (set-val! :config r)
                           (set-val! :trained? true)
                           (update-filter (:filter-old s))
                           (set-val! :features-old (:features s))
                           (test-config))})))


(defn action-button [pre-applied text1 text2 apply-fn]
  [:div
   {:style {:padding-top "1.3em"}}
   (if (pre-applied @state)
     [:button.form-control.btn.btn-success {:disabled 1} text2]
     [:button.form-control.btn.btn-primary {:on-click apply-fn} text1])])

(defn train-button [pre-applied text1 text2 apply-fn]
  (if-not (pre-applied @state)
    (if (enough-labels?)
      [:button.form-control.btn.btn-primary {:on-click apply-fn} text1]
      [:button.form-control.btn.btn-primary {:disabled 1}
       (if (:filter-applied? @state)
         "Please label more data!"
         "Please fetch something!")])
    [:button.form-control.btn.btn-success {:disabled 1} text2]))

(defn code [id on-change rows enabled]
  [:textarea.form-control
   {:id id
    :style {:margin-top "1em"}
    :rows rows
    :value (id @state)
    :on-change #(on-change (-> % .-target .-value))
    :disabled (not enabled)}
   ])

(defn selector [t k v]
  (letfn [(ha! [num]
            ;; (prn k num)
            (swap! state assoc-in [:labels k] num)
            (set-val! :trained? false))]
    [:tr {:class (if (= v 1)
                   "danger"
                   (if (= v 2)
                     "success"))}
     [:td.text-center
      [:div.btn-group {:data-toggle "buttons"}
       [:label.btn {:on-click #(ha! 2) :title "Green"}
        [:input {:type "radio" :name (str "opt" k)}] "G"]
       [:label.btn.active {:on-click #(ha! 0) :title "Clear label"}
        [:input {:type "radio" :name (str "opt" k) :checked 1}] "N"]
       [:label.btn {:on-click #(ha! 1) :title "Red"}
        [:input {:type "radio" :name (str "opt" k)}] "R"]
       ]]
     [:td t]]))

(defn label-list []
  (let [ts (:tweets @state)
        ls (:labels @state)]
    [:div.table-responsive.scrollme
     [:table.table.table-hover.table-striped.table-condensed
      [:thead
       [:tr
        [:th.text-center.col-md-2
         [:span.text-success "Green "]
         " None "
         [:span.text-danger " Red"]]
        [:th "Tweet"]]]
      [:tbody
       (if (= 0 (count ts))
         [:tr {:rowspan 5}
          [:td.text-center {:colspan 2}
           [:div.text-center [:h2 [:span.help-block "No Data"]]]]]
         (for [[t [k v]] (zipmap ts ls)]
           [selector t k v]))]]]))

(defn result-row [t v]
  [:tr {:class (if (= v 1)
                 "danger"
                 (if (= v 2)
                   "success"))}
   [:td t]])

(defn result-list []
  [:div.table-responsive.scrollme2
   [:table.table.table-hover.table-striped.table-condensed
    [:tbody
     (for [[t v] (:tested @state)]
       [result-row t v])]]])

(defn debug [k]
  [:div
   (str (k @state))])

(defn stats []
  [:div
   (str (:weights (:config @state)))])

(defn home []
  [:div.page
   [:div.page-header [:h3
                        [:span {:style {:color "green"}} "log"]
                        [:span {:style {:color "red"}} "reg"]
                        [:span {:style {:color "black"}} "dog"]
                      ]]
   [:div#row1
    [:h4 "Condition Filter"]
    [:div.help-block.gr (:filter help)]
    [code :filter update-filter 14 true]
    [action-button :filter-applied? "Go, Fetch!" "Data Fetched" apply-filter]
    ]
   [:div#row2
    [:h4 "Features"]
    [:div.help-block.gr (:features help)]
    [code :features update-features 16 (:features-enabled @state)]
    ]

   [:div#row-right
    [:ul.nav.nav-tabs
     [:li.active [:a {:href "#train" :data-toggle "tab"} "Train"]]
     [:li [:a {:href "#test" :data-toggle "tab"} "Test"]]
     [:li [:a {:href "#stats" :data-toggle "tab"} "Stats"]]]
    [:div.tab-content
     [:div#train.tab-pane.in.active
      [label-list]
      [train-button :trained? "Train!" "Trained" train]]
     [:div#test.tab-pane
      [:div.panel.panel.default
       [:div.panel-heading
        [:span.help-block "Your labeled data, together with your 
features, were used to train an AI to separate the tweets
into the two categories you imagined, automatically.
Here you can try how it works on some freshly tweeted tweets.
These tweets will also be subjected to the same condition filter
as your tweets for training were: that is a very important concept
in machine learning."]
        [:button.form-control.btn.btn-default
         {:type "button"
          :on-click test-config
          :data-toggle "tooltip"
          :data-placement "bottom"
          :data-original-title "qweqewqwe"}
         "Test Again"]]
       [:div.panel-body
        [result-list]]]
      ]
     [:div#stats.tab-pane
      [:div.panel.panel.default
       [:div.panel-heading
        [:p "Below are the weights of your features, in the
same order as the order of your features. Ignore if they are positive
or negative. The important is their absolute value. The more the absolute
value is bigger, the more a feature has been useful to the classifier.
This gives you precious feedback. You might want to make more features
similar to your good ones. Or you might want to see for bugs in a bad one
of which you had high expectations."]]
       [:div.panel-body
        [stats]]]]]]])

;; start the app
(reagent/render-component [home] (.getElementById js/document "app"))
