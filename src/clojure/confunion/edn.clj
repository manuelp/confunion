(ns confunion.edn
  (:require [clojure.edn :as edn]))

(defn load-edn
  "Reads an EDN data structure from the file located at the given `path`.
  Note that if the given file doesn't exists, an exception is thrown."
  [path]
  (edn/read-string (slurp path)))
