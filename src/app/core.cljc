(ns app.core
  (:require [app.back-pressure]
            [app.chat]
            [app.chat-extended]
            [app.component-lifecycle]
            [app.reagent-interop]
            [app.seven-guis-counter]
            [app.seven-guis-crud]
            [app.seven-guis-temperature-converter]
            [app.seven-guis-timer]
            [app.svg]
            [app.system-properties]
            [app.todo-list]
            [app.toggle]
            [app.two-clocks]
            [app.webview]
            [clojure.string :as str]
            [contrib.ednish]
            [contrib.str]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.history :as history]))

(e/defn NotFound []
  (dom/text "Not Found"))

(e/def routes
  {'todo-list                        {::page   app.todo-list/Page
                                      ::title  "Todo-list"
                                      ::source "https://github.com/hyperfiddle/electric-starter-app"}
   'two-clocks                       {::page   app.two-clocks/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-two-clocks!TwoClocks"
                                      ::title  "Two Clocks"}
   'toggle                           {::page   app.toggle/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-toggle!Toggle"
                                      ::title  "Toggle"}
   'system-properties                {::page   app.system-properties/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-system-properties!SystemProperties"
                                      ::title  "System Properties"}
   'chat                             {::page   app.chat/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-chat!Chat"
                                      ::title  "Chat"}
   'back-pressure                    {::page   app.back-pressure/Page
                                      ::source "https://electric.hyperfiddle.net/user.tutorial-backpressure!Backpressure"
                                      ::title  "Back Pressure"}
   'component-lifecycle              {::page   app.component-lifecycle/Page
                                      ::source "https://electric.hyperfiddle.net/user.tutorial-lifecycle!Lifecycle"
                                      ::title  "Component Lifecycle"}
   'chat-extended                    {::page   app.chat-extended/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-chat-extended!ChatExtended"
                                      ::title  "Chat Extended"}
   'webview                          {::page   app.webview/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-webview!Webview"
                                      ::title  "Webview"}
   'reagent-interop                  {::page   app.reagent-interop/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-reagent-interop!ReagentInterop"
                                      ::title  "Reagent Interop"}
   'svg                              {::page   app.svg/Page
                                      ::source "https://electric.hyperfiddle.net/user.demo-svg!SVG"
                                      ::title  "SVG"}
   'seven-guis-counter               {::page   app.seven-guis-counter/Page
                                      ::source "https://eugenkiss.github.io/7guis/tasks#counter"
                                      ::title  "Seven GUIs Counter"}
   'seven-guis-temperature-converter {::page   app.seven-guis-temperature-converter/Page
                                      ::source "https://eugenkiss.github.io/7guis/tasks#counter"
                                      ::title  "Seven GUIs Temperature Converter"}
   'seven-guis-timer                 {::page   app.seven-guis-timer/Page
                                      ::source "https://eugenkiss.github.io/7guis/tasks#counter"
                                      ::title  "Seven GUIs Timer"}
   'seven-guis-crud                  {::page   app.seven-guis-crud/Page
                                      ::source "https://eugenkiss.github.io/7guis/tasks#counter"
                                      ::title  "Seven GUIs CRUD"}})

(e/defn Nav []
  (dom/ul
    (dom/li (history/link ['todo-list] {} (dom/text "Todo-list")))
    (dom/li (history/link ['two-clocks] {} (dom/text "Two clocks")))
    (dom/li (history/link ['toggle] {} (dom/text "Toggle")))
    (dom/li (history/link ['system-properties] {} (dom/text "System Properties")))
    (dom/li (history/link ['chat] {} (dom/text "Chat")))
    (dom/li (history/link ['back-pressure] {} (dom/text "Back Pressure")))
    (dom/li (history/link ['component-lifecycle] {} (dom/text "Component Lifecycle")))
    (dom/li (history/link ['chat-extended] {} (dom/text "Chat Extended")))
    (dom/li (history/link ['webview] {} (dom/text "Webview")))
    (dom/li (history/link ['reagent-interop] {} (dom/text "Reagent Interop")))
    (dom/li (history/link ['svg] {} (dom/text "SVG")))
    (dom/li (history/link ['seven-guis-counter] {} (dom/text "Seven GUIs Counter")))
    (dom/li (history/link ['seven-guis-temperature-converter] {} (dom/text "Seven GUIs Temperature Converter")))
    (dom/li (history/link ['seven-guis-timer] {} (dom/text "Seven GUIs Timer")))
    (dom/li (history/link ['seven-guis-crud] {} (dom/text "Seven GUIs CRUD")))))

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
