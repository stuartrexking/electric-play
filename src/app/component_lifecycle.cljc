(ns app.component-lifecycle
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]))

(e/defn BlinkerComponent []
  (dom/h1 (dom/text "Sup!"))
  (println "I mounted")
  (e/on-unmount #(println "I've unmounted")))

(e/defn Page []
  (if (= 0 (int (mod e/system-time-secs 2)))
    (BlinkerComponent.)))