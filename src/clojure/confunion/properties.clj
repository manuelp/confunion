;; Since this library strives to also be used by Java, and in particular in a
;; context in which `java.util.Properties` objects are used for configuration,
;; we need a way to *import* into such objects the configuration we can read
;; from EDN files.

;; However, `java.util.Properties` are simple string to string dictionaries,
;; and they aren't as expressive as EDN data structures can be. If you aren't
;; constrained to `java.util.Properties` objects for compatibility reasons, you
;; can ignore this namespace and just use them. If, conversely, you need to
;; write the resulting configuration into them, here are some useful functions
;; to do just that.
(ns confunion.properties
  "This namespace contains the API to manipulate `java.util.Properties` objects."
  (:require [clojure.walk :as walk])
  (:import java.util.Properties))

(defn stringify-values
  "Recursively transforms all of map's values in EDN strings."
  [m]
  (let [f (fn [[k v]]
            (if (not (string? v))
              [k (pr-str v)]
              [k v]))]
    (walk/postwalk (fn [x]
                     (if (map? x)
                       (into {} (map f x))
                       x))
                   m)))

(defn propertize-map
  "Takes any map data structure and converts all of its keys and values to
  strings."
  [m]
  (-> m
      walk/stringify-keys
      stringify-values))

(defn add-properties
  "Adds/overwrites properties in a `java.util.Properties` object
  using a map `m`, which keys and values will be serialized in EDN
  strings."
  [props m]
  (do
    (doall (map #(.setProperty props (key %) (val %))
                (propertize-map m)))
    props))
