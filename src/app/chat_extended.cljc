(ns app.chat-extended
  (:require
    [contrib.str :as str]
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]))

#?(:clj (defonce !msgs (atom (list))))

(e/def msgs (e/server (reverse (e/watch !msgs))))

#?(:clj (defonce !present (atom {})))

(e/def present (e/server (e/watch !present)))

(e/defn ChatUI [username]
  (dom/div (dom/text "Present: "))
  (dom/ul
    (e/server
      (e/for-by first [[session-id username] present]
        (e/client
          (dom/li (dom/text username (str " (session-id: " session-id ")")))))))

  (dom/hr)
  (dom/ul
    (e/server
      (e/for [{:keys [::username ::msg]} msgs]
        (e/client
          (dom/li (dom/strong (dom/text username))
            (dom/text " " msg))))))


  (dom/input
    (dom/props {:placeholder "Type a message"})
    (dom/on "keydown"
      (e/fn [e]
        (when (= "Enter" (.-key e))
          (when-some [v (str/empty->nil (-> e .-target .-value))]
            (dom/style {:background-color "yellow"})
            (e/server
              (swap! !msgs #(cons {::username username ::msg v}
                              (take 9 %))))
            (set! (.-value dom/node) "")))))))

(e/defn Page []
  (let [session-id (e/server (get-in e/*http-request* [:headers "sec-websocket-key"]))
        username   (e/server (get-in e/*http-request* [:cookies "username" :value]))]
    (if-not (some? username)
      (dom/div
        (dom/text "Set login cookie here: ")
        (dom/a (dom/props {::dom/href "/auth"}) (dom/text "/auth"))
        (dom/text " (blank password)"))
      (do
        (e/server
          (swap! !present assoc session-id username)
          (e/on-unmount #(swap! !present dissoc session-id)))
        (dom/div (dom/text "Authenticated as: " username))
        (ChatUI. username)))))
