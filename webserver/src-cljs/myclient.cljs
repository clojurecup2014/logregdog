(ns myclient
  (:require [reagent.core         :as reagent]
            [ajax.core            :as ajax]
            [clojure.browser.repl :as repl]
  ))

;; listen to the ClojureScript REPL
(repl/connect "http://localhost:9000/repl")

(def state
  (reagent/atom
   {:doc {}
    :saved? false
    :filter "(defn condition [tweet] true)"
    :features "(defn feature_length [tweet]
  (/ (count tweet) 140))
"
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

(defn set-value! [id value]
  (swap! state assoc :saved? false)
  (swap! state assoc-in [:doc id] value))

(defn get-value [id]
  (get-in @state [:doc id]))

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
  (let [selections (->> items (map (fn [[k]] [k false])) (into {}) reagent/atom)]    
    (fn []
      [:div.row
       [:div.col-md-2 [:span label]]
       [:div.col-md-5
        [:div.row
         (for [[k v] items]
          [list-item id k v selections])]]])))

(defn save-doc []
  (ajax/POST (str js/context "/save")
        {:params (:doc @state)
         :handler (fn [_] (swap! state assoc :saved? true))}))

(defn home []
  [:div.page
   [:div.page-header [:h1 "Logistic Regression Dog"]]

   [:div#row1
    [:h2 "Condition Filter"]
    [:textarea.form-control
     {;;:on-change #(set-value! id (-> % .-target .-value))
      }
     (:filter @state)]
    [:span.help-block (:filter help)]]

   [:div#row2
    [:h2 "Features"]
    [:textarea.form-control
     {;;:on-change #(set-value! id (-> % .-target .-value))
      }
     (:features @state)]
    [:span.help-block (:features help)]]

   [:div#row-right
    [:ul.nav.nav-tabs
     [:li.active [:a {:href "#train"
                      :data-toggle "tab"} "Train"]]
     [:li [:a {:href "#test"
               :data-toggle "tab"} "Test"]]
     [:li [:a {:href "#stats"
               :data-toggle "tab"} "Stats"]]
     ]
    [:div.tab-content
     [:div#train.tab-pane.fade.in.active
      "train here"
      [text-input :first-name "First name"]
      [text-input :last-name "Last name"]
      [selection-list :favorite-drinks "Favorite drinks"
       [:coffee "Coffee"] [:beer "Beer"] [:crab-juice "Crab juice"]]
      
      (if (:saved? @state)
        [:p "Saved"]
        [:button {:type "submit"
                  :class "btn btn-default"
                  :on-click save-doc}
         "Submit"])]
     [:div#test.tab-pane.fade "test adj jaksdhjk ahd "]
     [:div#stats.tab-pane.fade "stats ajkdh kjashd kjah"]
     ]


    
    ]])

;; start the app
(reagent/render-component [home] (.getElementById js/document "app"))

