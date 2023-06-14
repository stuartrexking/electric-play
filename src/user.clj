(ns user
  (:require [clojure.repl.deps :as deps]))

;faster repl start
(def start-server! (delay @(requiring-resolve 'core/start!)))
(def stop-server! (delay @(requiring-resolve 'core/stop!)))
(def shadow-start! (delay @(requiring-resolve 'shadow.cljs.devtools.server/start!)))
(def shadow-watch (delay @(requiring-resolve 'shadow.cljs.devtools.api/watch)))

(defn main [& _]
  (@shadow-start!)
  (@shadow-watch :dev)
  (@start-server!)
  (comment (@stop-server)))

(comment
  ;; all you need to run
  (main)

  ;; don't really need these
  (hyperfiddle.rcf/enable!)
  (shadow.cljs.devtools.api/repl :dev)

  ;;sync deps to classpath
  (deps/sync-deps)
  *e)