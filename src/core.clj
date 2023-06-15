(ns core
  (:require
    [hyperfiddle.electric-jetty-adapter :as adapter]
    [reitit.ring :as ring]
    [ring.adapter.jetty9 :as jetty]
    [ring.middleware.basic-authentication :as auth]
    [ring.middleware.content-type :as content-type]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.params :as params]
    [ring.util.response :as response])
  (:import (org.eclipse.jetty.server.handler.gzip GzipHandler)))
(def ^:const VERSION (not-empty (System/getProperty "HYPERFIDDLE_ELECTRIC_SERVER_VERSION")))

;;middleware
(defn wrap-electric-websocket [handler]
  (fn [req]
    (if (jetty/ws-upgrade-request? req)
      (->> req
        (partial adapter/electric-ws-message-handler)
        (adapter/electric-ws-adapter)
        (jetty/ws-upgrade-response))
      (handler req))))

(defn wrap-reject-stale-client [handler]
  (fn [req]
    (if (jetty/ws-upgrade-request? req)
      (let [client-version (get-in req [:query-params "HYPERFIDDLE_ELECTRIC_CLIENT_VERSION"])]
        (cond
          (nil? VERSION) (handler req)
          (= client-version VERSION) (handler req)
          :else (adapter/reject-websocket-handler 1008 "stale client")))
      (handler req))))

(defn electric-websocket-middleware [handler]
  (-> handler
    (wrap-electric-websocket)
    (cookies/wrap-cookies)
    (wrap-reject-stale-client)
    (params/wrap-params)))

(defn index-handler [_]
  (-> "public/index.html"
    (response/resource-response)
    (response/content-type "text/html")
    (response/header "Cache-Control" "no-store")))

;;fake auth function
(defn authenticate [username password]
  username)

(defn authentication-middleware [handler]
  (fn [req]
    (let [res (handler req)]
      (if-let [username (:basic-authentication req)]
        (response/set-cookie res "username" username {:http-only true})
        res))))

;;router
(def ring-handler
  (ring/ring-handler
    (ring/router
      [["/assets/*" (ring/create-resource-handler)]
       ["/auth" {:get        (fn [req]
                               (if-let [username (:basic-authentication req)]
                                 (let [referer (get-in req [:headers "referer"] "http://localhost:58080/chat-extended")]
                                   (-> referer
                                     (response/redirect)
                                     (response/set-cookie "username" username {:http-only true})))
                                 (response/not-found "Where is the missile?")))
                 :middleware [[auth/wrap-basic-authentication authenticate]
                              [cookies/wrap-cookies]
                              [authentication-middleware]]}]]
      {:data {:middleware [[content-type/wrap-content-type]]}})
    index-handler
    {:middleware [[electric-websocket-middleware]]}))

;;http server
(defonce !server (atom nil))

(defn stop! []
  (when-let [server @!server]
    (jetty/stop-server server)
    (reset! !server nil)
    :stopped))

(defn- configurator [server]
  (let [server-handler (.getHandler server)
        gzip-handler   (doto (GzipHandler.)
                         (.setMinGzipSize 1024)
                         (.setHandler server-handler))]
    (.setHandler server gzip-handler)))

(defn start! []
  (stop!)
  (let [;waiting on https://github.com/sunng87/ring-jetty9-adapter/issues/102
        ;ring-handler (var ring-handler)
        server (jetty/run-jetty ring-handler {:configurator configurator
                                              :join?        false
                                              :port         58080})
        port   (-> server (.getConnectors) (first) (.getPort))]
    (println (format "\n🦡 Honey Badger running on port %s" port "\n"))
    (reset! !server server)
    :started))

(defn restart! []
  (stop!)
  (start!))

(comment

  ;;start or stop http server
  (start!)
  (stop!)
  (restart!)

  *e)