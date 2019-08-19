(ns com.benizi.edn
  (:require [clojure.edn :as edn]
            [cheshire.core :as json])
  (:import java.util.UUID)
  (:gen-class))

(def ^:dynamic *uuids* (atom {}))

(defn cache-or-generate
  [v]
  (or v (UUID/randomUUID)))

(defn uuid-type [v]
  (cond (string? v) :read
        (keyword? v) :cache
        (number? v) :cache
        (symbol? v) :cache
        (vector? v) :literal
        :else :generate))

(defmulti read-uuid
  "Read a UUID with special handling for certain 'value' types"
  uuid-type)

(defmethod read-uuid :read [v] (UUID/fromString v))
(defmethod read-uuid :generate [_] (UUID/randomUUID))
(defmethod read-uuid :cache [k]
  (-> *uuids*
      (swap! update k cache-or-generate)
      (get k)))
(defmethod read-uuid :literal [[value len]]
  (let [total-len 32
        len (or len total-len)
        hex-val (String/format (str "%0" len "x") (object-array [value]))
        actual-len (count hex-val)
        need (Math/ceil (/ total-len actual-len))
        extra-len (rem (* need actual-len) total-len)
        hexits (drop extra-len (apply str (repeat need hex-val)))
        groups (mapcat conj (partition 4 hexits) [nil nil \- \- \- \- nil nil])
        hyphenated (apply str groups)]
    (UUID/fromString hyphenated)))

(defn edn-read
  "Wrapper for clojure.edn/read to handle certain `#uuid value` types specially

  - keyword, number, or symbol:
    - if not yet seen, generate a random UUID
    - if seen previosly, return the previously-generated UUID
  - vector `[value]` 'literal' UUID
    - same as `[value 32]`
  - vector `[value hex-length]`
    - format `value` as hex of length `hex-length`, then repeat it to fill a
      UUID.

  E.g.:

  ```clojure
  com.benizi.edn=> (defn r [s] (with-in-str s (com.benizi.edn/edn-read *in*)))

  com.benizi.edn=> (r \"[#uuid 1 #uuid 2 #uuid 1]\")
  [
    #uuid \"30d4ed57-031b-4e69-9914-17079e177d22\" ;; this one ...
    #uuid \"4d94194a-2138-4224-81eb-7906dd10797d\"
    #uuid \"30d4ed57-031b-4e69-9914-17079e177d22\" ;; ... equal to this one
  ]

  com.benizi.edn=> (r \"{:a {:id #uuid :a} :b {:a-id #uuid :a}}\")
  {:a {:id #uuid \"7ed403ed-9c36-48d9-a43b-24e9c4d4c571\"},
   :b {:a-id #uuid \"7ed403ed-9c36-48d9-a43b-24e9c4d4c571\"}}

  com.benizi.edn=> (r \"{:a {:id #uuid -1} :b {:a-id #uuid -1}}\")
  {:a {:id #uuid \"2aa5b401-6ced-4cc8-ae2d-ae3e0c165af9\"},
   :b {:a-id #uuid \"2aa5b401-6ced-4cc8-ae2d-ae3e0c165af9\"}}

  com.benizi.edn=> (r \"#uuid [1]\")
  #uuid \"00000000-0000-0000-0000-000000010001\"

  com.benizi.edn=> (r \"#uuid [1 4]\")
  #uuid \"00010001-0001-0001-0001-000100010001\"

  com.benizi.edn=> (r \"#uuid [1 3]\")
  #uuid \"01001001-0010-0100-1001-001001001001\"
  ```
  "
  [in]
  (let [readers (assoc default-data-readers 'uuid read-uuid)]
    (edn/read {:eof ::eof :readers readers} *in*)))

(defn -main
  "Loop over input reading EDN data, outputting JSON"
  [& args]
  (if (seq args)
    (with-in-str (apply str (interpose " " args)) (-main))
    (try
      (loop []
        (let [o (edn-read *in*)]
          (when (not= o ::eof)
            (println (json/encode o))
            (recur))))
      (catch Exception e
        (binding [*out* *err*]
          (println (str "Error: " (.getMessage e))))))))
