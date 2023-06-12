(ns core
  (:require
    [aleph.http :as http]
    [clojure.repl.deps :as deps]
    [reitit.ring :as ring]
    [ring.middleware.content-type :as content-type]
    [ring.util.response :as response]
    [shadow.cljs.devtools.api :as shadow]
    [shadow.cljs.devtools.server :as server]))

;;router
(def ring-handler
  (ring/ring-handler
    (ring/router
      [["/" {:get {:handler (fn [_]
                              (-> "Meow!"
                                (response/response)
                                (response/content-type "text/plain")))}}]
       ["/assets/*" (ring/create-resource-handler)]]
      {:data {:middleware [[content-type/wrap-content-type]]}})
    (constantly {:status 404, :body "Oof!"})))

;;http server
(defonce !closeable (atom nil))

(defn stop! []
  (when-let [closeable @!closeable]
    (.close closeable)
    (reset! !closeable nil)
    :stopped))

(defn start! []
  (stop!)
  (let [ring-handler (var ring-handler)
        closeable    (http/start-server ring-handler {:port             3000
                                                      :shutdown-timeout 5})]
    (println (str "Running on port 3000"))
    (reset! !closeable closeable)
    :started))

(defn restart! []
  (stop!)
  (start!))

(comment
  ;;sync deps to classpath
  (deps/sync-deps)

  ;;start or stop http server
  (start!)
  (stop!)
  (restart!)

  ;;shadow server
  (server/start!)
  (server/stop!)
  (shadow/compile :dev)
  (shadow/watch :dev)
  (shadow/release :dev)

  *e)