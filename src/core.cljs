(ns core)

(defn ^:dev/after-load ^:export start! []
  (println "start!"))

(defn ^:dev/before-load stop! []
  (println "stop!"))

