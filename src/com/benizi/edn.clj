(ns com.benizi.edn
  (:require [clojure.edn :as edn]
            [cheshire.core :as json])
  (:gen-class))

(defn -main
  "Loop over input reading EDN data, outputting JSON"
  [& args]
  (loop []
    (let [eof :com.benizi.edn/eof
          o (edn/read {:eof eof} *in*)]
      (when (not= o eof)
        (println (json/encode o))
        (recur)))))
