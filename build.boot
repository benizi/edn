#!/usr/bin/env boot

(require '[clojure.java.io :as io])

(set-env!
  :resource-paths #{"src"}
  :dependencies `[[org.clojure/clojure ~(clojure-version) :scope "provided"]
                  [cheshire/cheshire "5.8.0"]])

(def main-ns 'com.benizi.edn)

(deftask uberjar
  "Build standalone JAR file."
  []
  (comp
    (aot :namespace #{main-ns})
    (uber)
    (jar :file (str main-ns ".jar") :main main-ns)
    (sift :include #{#".jar$"})
    (target)))

(defn native-image-cmd
  "Construct the commandline to compile natively with Graal."
  [jar-path out-name]
  ["native-image"
   "-H:+ReportUnsupportedElementsAtRuntime"
   "--no-server"
   "-jar" jar-path
   (str "-H:Name=" out-name)])

(defn native-image
  "Run Graal `native-image` to create a compiled executable."
  [in-path out-path]
  (let [out-dir (.getParent out-path)
        out-name (.getName out-path)]
    (binding [*sh-dir* out-dir]
      (apply dosh (native-image-cmd in-path out-name)))))

(defn find-uberjar
  "Find the path to the only .jar file in a fileset."
  [fileset]
  (->> fileset
       input-files
       (by-ext [".jar"])
       first
       tmp-file
       .getPath))

(deftask native
  "Build native binary."
  [o outfile FILE str "The name of the binary to build."]
  (comp
    (with-pre-wrap fileset
      (let [tgt (tmp-dir!)]
        (empty-dir! tgt)
        (let [uberjar-path (find-uberjar fileset)
              out-base (or outfile "edn")
              out-path (io/file tgt out-base)]
          (native-image uberjar-path out-path))
        (-> fileset
            (add-resource tgt)
            commit!)))
    (target)))

(deftask build
  "Compile uberjar and build native binary."
  [o outfile FILE str "The name of the binary to build."]
  (comp
    (uberjar)
    (native :outfile outfile)))

#_(;; TODO
(require '[boot.task.built-in :as original])
(ns-unmap 'boot.user 'install)
(deftask install
  []
  (with-post-wrap fileset
    (io
      )))
)

(defn -main [& args]
  (require main-ns)
  (let [main (->> [main-ns '-main]
                  (map str)
                  (apply symbol)
                  resolve)]
    (apply main args)))
