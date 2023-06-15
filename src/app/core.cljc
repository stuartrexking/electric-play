(ns app.core
  (:require [app.back-pressure]
            [app.chat]
            [app.chat-extended]
            [app.component-lifecycle]
            [app.system-properties]
            [app.todo-list]
            [app.toggle]
            [app.two-clocks]
            [clojure.string :as str]
            [contrib.ednish]
            [contrib.str]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.history :as history]))

(e/defn NotFound []
  (dom/text "Not Found"))

(e/def routes
  {'todo-list           {::page   app.todo-list/Page
                         ::title  "Todo-list"
                         ::source "https://github.com/hyperfiddle/electric-starter-app"}
   'two-clocks          {::page   app.two-clocks/Page
                         ::source "https://electric.hyperfiddle.net/user.demo-two-clocks!TwoClocks"
                         ::title  "Two Clocks"}
   'toggle              {::page   app.toggle/Page
                         ::source "https://electric.hyperfiddle.net/user.demo-toggle!Toggle"
                         ::title  "Toggle"}
   'system-properties   {::page   app.system-properties/Page
                         ::source "https://electric.hyperfiddle.net/user.demo-system-properties!SystemProperties"
                         ::title  "System Properties"}
   'chat                {::page   app.chat/Page
                         ::source "https://electric.hyperfiddle.net/user.demo-chat!Chat"
                         ::title  "Chat"}
   'back-pressure       {::page   app.back-pressure/Page
                         ::source "https://electric.hyperfiddle.net/user.tutorial-backpressure!Backpressure"
                         ::title  "Back Pressure"}
   'component-lifecycle {::page   app.component-lifecycle/Page
                         ::source "https://electric.hyperfiddle.net/user.tutorial-lifecycle!Lifecycle"
                         ::title  "Component Lifecycle"}
   'chat-extended       {::page   app.chat-extended/Page
                         ::source "https://electric.hyperfiddle.net/user.demo-chat-extended!ChatExtended"
                         ::title  "Chat Extended"}})

(e/defn Nav []
  (dom/ul
    (dom/li (history/link ['todo-list] {} (dom/text "Todo-list")))
    (dom/li (history/link ['two-clocks] {} (dom/text "Two clocks")))
    (dom/li (history/link ['toggle] {} (dom/text "Toggle")))
    (dom/li (history/link ['system-properties] {} (dom/text "System Properties")))
    (dom/li (history/link ['chat] {} (dom/text "Chat")))
    (dom/li (history/link ['back-pressure] {} (dom/text "Back Pressure")))
    (dom/li (history/link ['component-lifecycle] {} (dom/text "Component Lifecycle")))
    (dom/li (history/link ['chat-extended] {} (dom/text "Chat Extended")))))

(e/defn Router []
  (if-let [route (get routes (first history/route))]
    (let [{::keys [page source title]} route]
      (set! (.-title js/document) title)
      (Nav.)
      (dom/div
        (dom/a (dom/props {:href source}) (dom/text source)))
      (dom/br)
      (new page))
    (NotFound.)))

(defn route->path [route]
  (clojure.string/join "/" (map contrib.ednish/encode-uri route)))

(defn path->route [s]
  (let [s (contrib.ednish/discard-leading-slash s)]
    (case s
      "" nil
      (->> (str/split s #"/") (mapv contrib.ednish/decode-uri)))))

(e/defn Page []
  ;(dom/text "hello")
  (binding [history/encode route->path
            history/decode path->route]
    (history/router (history/HTML5-History.)
      (binding [dom/node js/document.body]
        (Router.)))))