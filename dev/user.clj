(ns user
  (:require [gs3.core :as gs3]))

(defn nrepl-handler []
  (let [cider-handler (requiring-resolve 'cider.nrepl/cider-nrepl-handler)
        wrap-refactor (requiring-resolve 'refactor-nrepl.middleware/wrap-refactor)]
    (-> cider-handler
        wrap-refactor)))

(defn cider-nrepl
  []
  (.start
   (Thread.
    (fn []
      (let [start-server (requiring-resolve 'nrepl.server/start-server)]
        (start-server :port 7888 :handler (nrepl-handler))
        (println "nREPL server started on port 7888"))))))

(defn go
  []
  (cider-nrepl)
  (gs3/run))
