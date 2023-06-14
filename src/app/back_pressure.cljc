(ns app.back-pressure
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom])
  (:import (hyperfiddle.electric Pending)))

(e/defn Page []
  (let [c (e/client e/system-time-secs)
        s (e/server (double e/system-time-secs))]
    (dom/div (dom/text "client time: " c))
    (dom/div (dom/text "server time: " s))
    (dom/div (dom/text "difference: " (.toPrecision (- c s) 5)))
    (dom/div (dom/text (if (> c s) "Client Ahead" "Server Ahead")))))
