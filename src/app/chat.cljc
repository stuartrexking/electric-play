(ns app.chat
  (:require
    [contrib.data :as data]
    [contrib.str :as c.str]
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom])
  (:import (hyperfiddle.electric Pending)))

#?(:clj (defonce !messages (atom (list "The missile knows where it is at all times"))))

(e/def messages (e/server (data/pad 10 nil (e/watch !messages))))

(e/defn Page []
  (try
    (dom/ul
      (e/for-by identity [message (reverse messages)]
        (e/client
          (dom/li (dom/style {:visibility (if (nil message) "hidden" "visible")})
            (dom/text message)))))

    (dom/input
      (dom/props {:placeholder "Type a message"})
      (dom/on "keydown"
        (e/fn [e]
          (when (= "Enter" (.-key e))
            (when-some [v (c.str/empty->nil (.. e -target -value))]
              (e/server (swap! !messages #(cons v (take 9 %))))
              (set! (.-value dom/node) ""))))))

    (catch Pending e
      (dom/style {:background-color "yellow"}))))
