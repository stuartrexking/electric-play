(ns app.system-properties
  (:require
    [clojure.string :as str]
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]
    [hyperfiddle.electric-ui4 :as ui]))

#?(:clj
   (defn jvm-system-properties [?s]
     (->> (System/getProperties)
       (filter (fn [^java.util.concurrent.ConcurrentHashMap$MapEntry kv]
                 (str/includes?
                   (str/lower-case (str (key kv)))
                   (str/lower-case (str ?s)))))
       (sort-by key))))

(e/defn Page []
  (let [!search (atom "vm")
        search  (e/watch !search)]
    (e/server
      (let [system-properties (jvm-system-properties search)
            matched-count     (count system-properties)]
        (e/client
          (dom/div (dom/text matched-count " matches"))
          (ui/input search (e/fn [v]
                             (reset! !search v)))
          (dom/table
            (dom/tbody
              (e/server
                (e/for-by key [[k v] system-properties]
                  (e/client
                    (dom/tr
                      (dom/td (dom/text k))
                      (dom/td (dom/text v)))))))))))))
