(ns app.seven-guis-timer
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Page []
  (dom/text "Seven GUIs Timer"))