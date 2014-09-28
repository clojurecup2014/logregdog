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
   {:filter "(defn condition [tweet] true)"
    :filter-applied? false
    :features "(defn feature_length [tweet]
  (/ (count tweet) 140))

(defn has_a [tweet]
  (if (.contains tweet \"a\") 1 0))"
    :trained? false
    }))

(def help
  {:filter "This is filter function determining what tweets you get
use it to get only english by cheching for \"the\".
Default one lets all tweets.
Takes a string and returns bool or nil.
"
   :features "These are some sample feature functions.
They take string and return number between 0 and 1.
bla bla bla
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

(defn apply-filter []
  (let [ffun (:filter @state)]
    (ajax/POST (str js/context "/filtered-tweets")
               {:params {:filter-fun ffun
                         :delayed false
                         :max 10}
                :handler (fn [r]
                           ;; (prn r)
                           (set-val! :trained? false)
                           (set-val! :filter-applied? true)
                           (set-val! :filter-old ffun)
                           (set-val! :tweets r)
                           (set-labels! r))})))

(defn train []
  (let [s @state]
    (ajax/POST (str js/context "/train")
               {:params {:features (:features s)
                         :labels (map (fn [[k v] t]
                                        (list t v))
                                      (:labels s)
                                      (:tweets s))}
                :handler (fn [r]
                           (prn r)
                           (set-val! :config r)
                           (set-val! :trained? true)
                           (update-filter (:filter-old s))
                           (set-val! :features-old (:features s)))})))

(defn action-button [pre-applied text1 text2 apply-fn]
  (if (pre-applied @state)
      [:button.form-control.btn.btn-success {:disabled 1} text2]
      [:button.form-control.btn.btn-primary {:on-click apply-fn} text1]))

(defn code [id on-change rows]
  [:textarea.form-control
   {:id "code"
    :rows rows
    :value (id @state)
    :on-change #(on-change (-> % .-target .-value))}
   ])

(defn selectors [t k v]
  (letfn [(ha! [num]
            (prn k num)
            (swap! state assoc-in [:labels k] num))]
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
    [:div.table-responsive
     [:table.table.table-hover.table-striped
      [:thead
       [:tr [:th.text-center "Green None Red"] [:th "Tweet"]]]
      [:tbody
       (for [[t [k v]] (zipmap ts ls)]
         [selectors t k v])]]]))

(defn debug [k]
  [:div
   (str (k @state))])

(defn home []
  [:div.page
   [:div.page-header [:h1 
                        [:span {:style {:color "green"}} "log"]
                        [:span {:style {:color "red"}} "reg"]
                        [:span {:style {:color "black"}} "dog"]
                      ]]
   [debug :labels]
   [:div#row1
    [:h3 "Condition Filter"]
    [code :filter update-filter 8]
    [action-button :filter-applied? "Apply" "Applied" apply-filter]
    [:span.help-block (:filter help)]]
   [:div#row2
    [:h3 "Features"]
    [code :features update-features 10]
    [:span.help-block (:features help)]]

   [:div#row-right
    [:ul.nav.nav-tabs
     [:li.active [:a {:href "#train" :data-toggle "tab"} "Train"]]
     [:li [:a {:href "#test" :data-toggle "tab"} "Test"]]
     [:li [:a {:href "#stats" :data-toggle "tab"} "Stats"]]]
    [:div.tab-content
     [:div#train.tab-pane.fade.in.active
      [label-list]
      [action-button :trained? "Train!" "Trained" train]
      ;; [text-input :first-name "First name"]
      ;; [text-input :last-name "Last name"]
      ;; [selection-list :favorite-drinks "Favorite drinks"
      ;;  [:coffee "Coffee"] [:beer "Beer"] [:crab-juice "Crab juice"]]
      
      ;; (if (:saved? @state)
      ;;   [:p "Saved"]
      ;;   [:button {:type "submit"
      ;;             :class "btn btn-default"
      ;;             :on-click save-doc}
      ;;    "Submit"])
      ]
     [:div#test.tab-pane.fade "test adj jaksdhjk ahd "]
     [:div#stats.tab-pane.fade "stats ajkdh kjashd kjah"]
     ]]])

;; start the app
(reagent/render-component [home] (.getElementById js/document "app"))


