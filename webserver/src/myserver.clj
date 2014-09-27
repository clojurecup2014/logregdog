(ns myserver
  (:require 
            ; from lib-noir and its deps:
            [noir.response                   :as resp]
            [noir.util.middleware            :as middleware]
            [compojure.core                  :as compojure]
            [compojure.route                 :as route]
            [ring.middleware.file-info       :as ringfi]
            [ring.middleware.file            :as ringf]

            ; from other deps:
            [ring.server.standalone          :as standalone]
            [taoensso.timbre                 :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
  ))



;; math stuff here:

(def labeleditems '(["1" "SHORT"], ["11" "LONG"], 
                    ["2" "SHORT"], ["22" "LONG"], 
                    ["3" "SHORT"], ["33" "LONG"], 
                    ["4" "SHORT"], ["44" "LONG"], 
                    ["5" "SHORT"], ["55" "LONG"], 
                    ["6" "SHORT"], ["66" "LONG"]))

(def maxbad 0.1)

(defn perfect_feature [item] (* 0.1 (.length item)))

(defn useless_feature [item] (* 0.1 (java.lang.Integer/parseInt (.substring item 0 1))))

(def features [perfect_feature useless_feature])

(def classifier {:one "LONG" 
                 :zero "SHORT"
                 :intercept 90.42694206220736
                 :weights [-510.0369285921699 -29.78327218110678]
                 :averages [0.15 0.35]
                 })

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

;; web stuff below:

(defn save-document [doc]
  (timbre/info (str doc))
  {:status "ok"})

(compojure/defroutes app-routes
  (compojure/GET "/" [] (slurp "app.html"))
  (compojure/POST "/save" {:keys [body-params]} (resp/edn (save-document body-params)))
  (route/resources "/")
  (route/not-found "Not Found"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "reagent_example.log" :max-size (* 1024 1024) :backlog 10})
  (timbre/info "reagent-example started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "reagent-example is shutting down..."))

(def app (middleware/app-handler
           ;; add your application routes here
           [app-routes]
           ;; add custom middleware here
           :middleware ()
           ;; timeout sessions after a month
           :session-options {:timeout (* 60 60 24 30) :timeout-response (resp/redirect "/")}
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:edn]))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (ringf/wrap-file "resources")
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      (ringfi/wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [port]
  (reset! server
          (standalone/serve (get-handler)
                   {:port port
                    :init init
                    :auto-reload? true
                    :destroy destroy
                    :join? false
                    :open-browser? false
                   }))
  (timbre/info (str "You can view the site at http://localhost:" port)))

;; for easy usage from the REPL:
(defn stop-server []
  (.stop @server)
  (reset! server nil))

;; for easy usage from the REPL:
(defn restart []
  (stop-server)
  (start-server 3000))

;; start the server when the REPL is started
;; (start-server 3000)

