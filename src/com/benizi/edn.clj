(ns com.benizi.edn
  (:require [clojure.edn :as edn]
            [cheshire.core :as json])
  (:gen-class))

(defn -main
  "Loop over input reading EDN data, outputting JSON"
  [& args]
  (try
    (loop []
      (let [o (edn/read {:eof ::eof} *in*)]
        (when (not= o ::eof)
          (println (json/encode o))
          (recur))))
    (catch Exception e
      (binding [*out* *err*]
        (println (str "Error: " (.getMessage e)))))))
