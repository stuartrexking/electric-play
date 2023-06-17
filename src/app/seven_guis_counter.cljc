(ns app.seven-guis-counter
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]))

(e/defn Page []
  (let [!state (atom 0)]
    (dom/p (dom/text (e/watch !state)))
    (ui/button (e/fn [] (swap! !state inc))
      (dom/text "Count"))))