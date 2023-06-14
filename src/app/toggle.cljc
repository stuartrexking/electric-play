(ns app.toggle
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]
    [hyperfiddle.electric-ui4 :as ui]))

;;server state, stores whether to use client or server
#?(:clj (defonce !x (atom true)))

;;reactive signal from atom
(e/def x (e/server (e/watch !x)))

(e/defn Page []
  (dom/div
    (dom/text "number type here is: "
      (case x
        true (e/client (pr-str (type 1)))
        false (e/server (pr-str (type 1))))))

  (dom/div
    (dom/text "current site: "
      (if x
        "ClojureScript (client)"
        "Clojure (server)")))

  (ui/button (e/fn []
               (e/server (swap! !x not)))
    (dom/text "toggle client/server")))

