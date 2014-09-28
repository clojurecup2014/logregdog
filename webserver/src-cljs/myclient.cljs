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
  (/ (count tweet) 140))"
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
  (let [selections (->> items (map (fn [[k]] [k false])) (into {}) reagent/atom)]    
    (fn []
      [:div.row
       [:div.col-md-2 [:span label]]
       [:div.col-md-5
        [:div.row
         (for [[k v] items]
          [list-item id k v selections])]]])))

(defn update-field [id id-old pre value]
  (prn id (id-old @state) value)
  (set-val! pre (= value (id-old @state)))
  (set-val! id value))

(def update-filter
  (partial update-field :filter :filter-old :filter-applied?))

(def update-features
  (partial update-field :features :features-old :trained?))

(defn apply-filter []
  (let [ffun (:filter @state)]
    (ajax/POST (str js/context "/save")
               {:params (:filter @state)
                :handler (fn [_]
                           (swap! state assoc :filter-applied? true)
                           (swap! state assoc :filter-old ffun))})))

(defn train []
  (let [s @state]
    (ajax/POST (str js/context "/save")
               {:params (:filter s)
                :handler (fn [_]
                           (swap! state assoc :trained? true)
                           (update-filter (:filter-old s))
                           (swap! state assoc :features-old (:features s)))})))

(defn action-button [pre-applied text1 text2 apply-fn]
  (if (pre-applied @state)
      [:button.form-control.btn.btn-success {:disabled 1} text2]
      [:button.form-control.btn.btn-primary {:on-click apply-fn} text1]))

(defn code [id on-change rows]
  [:textarea.form-control
   {:rows rows
    :value (id @state)
    :on-change #(on-change (-> % .-target .-value))}
   ])

(defn debug []
  [:div
   (str @state)])

(defn home []
  [:div.page
   [:div.page-header [:h1 "Logistic Regression Dog"]]

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
      "train here"
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

