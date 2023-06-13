(ns core
  (:require
    [aleph.http :as http]
    [aleph.http.server :as http.server]
    [clojure.repl.deps :as deps]
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric.impl.io :as io]
    [hyperfiddle.electric.impl.runtime :as r]
    [manifold.deferred :as d]
    [manifold.stream :as s]
    [missionary.core :as m]
    [reitit.ring :as ring]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.cookies :as cookies]
    [ring.util.response :as response]
    [shadow.cljs.devtools.api :as shadow]
    [shadow.cljs.devtools.server :as server])
  (:import (java.util UUID)))

(def non-websocket-request
  (-> "Expected a websocket request."
    (response/bad-request)
    (response/content-type "application/text")))

(defonce !connections (atom {}))

#_(defn consume-message [original-request conn message]
    (binding [e/*http-request* original-request]
      (let [resolvef (bound-fn [not-found x] (r/dynamic-resolve not-found x))]
        (e/eval resolvef
          (io/decode message))
        (prn resolvef)))

    (prn original-request)
    (prn message))

(defn close-connection [connection-id]
  (println (format "Connection %s closed" connection-id))
  (swap! !connections dissoc connection-id))

(defn electric-websocket-handler [req]
  (d/let-flow [conn (d/catch
                      (http/websocket-connection req)
                      (fn [_] nil))]
    (if-not conn
      ;This shouldn't ever return with this setup
      non-websocket-request

      (let [connection-id (str (UUID/randomUUID))]

        (s/on-closed conn (partial close-connection connection-id))

        (binding [e/*http-request* req]
          (let [resolve-m (bound-fn [not-found x] (r/dynamic-resolve not-found x))]
            (d/let-flow [encoded-program (s/take! conn)]
              (let [program    (io/decode encoded-program)
                    write-fn   (fn [m]
                                 (fn [s f]
                                   ;TODO: Error handling
                                   (println "write-fn")
                                   (prn m)
                                   (s/put! conn (io/encode m))
                                   #()))
                    read-fn    (fn [cb]
                                 (println "read-fn")
                                 (d/let-flow [m (s/take! conn)]
                                   (prn m)
                                   (cb (io/decode m))))
                    booting-fn (e/eval resolve-m program)]
                (m/sp
                  (m/?
                    (booting-fn write-fn read-fn))))))))))

  ;(s/consume (partial consume-message req conn) conn)

  ;(swap! !connections assoc connection-id conn))))
  ;(s/put! conn connection-id))))
  ;ring handler must return something
  nil)

(defn wrap-electric-websocket [handler]
  (fn [req]
    (if (http.server/websocket-upgrade-request? req)
      (electric-websocket-handler req)
      (handler req))))

(defn electric-websocket-middleware [handler]
  (-> handler
    (cookies/wrap-cookies)
    (wrap-electric-websocket)))

;;router
(def ring-handler
  (ring/ring-handler
    (ring/router
      [["/" {:get {:handler (fn [_]
                              (-> "public/index.html"
                                (response/resource-response)
                                (response/content-type "text/html")
                                (response/header "Cache-Control" "no-store")))}}]
       ["/assets/*" (ring/create-resource-handler)]]
      {:data {:middleware [[electric-websocket-middleware]
                           [content-type/wrap-content-type]]}})
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

  (def x (s/stream))

  (s/close! x)

  @(s/put! x :a)
  @(s/put! s :b)
  @(s/put! s :c)

  (s/take! s)

  (s/consume (fn [x] (prn x)) x)

  *e)