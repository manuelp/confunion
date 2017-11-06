(ns confunion.core
  "Core functions related to configuration data structures (reading, merging, etc.)"
  (:require [confunion.edn :refer [load-edn]]
            [clojure.tools.logging :as log]
            [confunion.schema :as schema]))

;; ## Configuration
;;
;; Confunion represents a configuration as an EDN map.

;; The heart of the library is *merging maps*. Luckily Clojure already ships with a generic
;; function to merge them in order.
(defn confunion
  "Merges EDN maps read from the list of `paths` specified, from left to right."
  [& paths]
  (apply merge (map load-edn paths)))

;; However, the high-level API of this library operates on *files* by their path.
;; So we need to check if a file exists given its path.
(defn file-exists?
  "Checks if the file referenced by a `path` exists."
  [path]
  (.exists (clojure.java.io/file path)))

;; The idea of this library is to build a configuration map, merging the content of a *base*
;; EDN file with another one, which contains *overrides* (this is the environment-specific
;; configuration).
;;
;; Since we want to expose at the high-level API a way to express an ordered sequence of
;; *possible paths* in which this override file *could* be found, we need a function to
;; check this sequence to found the first existing file (or nothing).
(defn first-existing
  "Returns the path of the first existing file, checking all the `paths` in order."
  [paths]
  (let [filtered (->> paths
                      (filter (comp not nil?))
                      (drop-while (comp not file-exists?)))]
    (if (empty? filtered)
      nil
      (do (log/info "File selected: " (first filtered) " between: " paths)
          (first filtered)))))

;; This is the main function that operates at the file paths level, which is
;; only concerned with *configuration* files.
(defn read-configuration
  "Builds an EDN configuration map merging the content of the first existing file in the
  `base-paths` ordered sequence, with the first existing one in `override-paths`."
  [base-paths override-paths]
  (let [base-path (first-existing base-paths)
        override-path (first-existing override-paths)]
    (cond (nil? base-path) (throw (Exception. "Base configuration file not found!"))
          (nil? override-path) (load-edn base-path)
          :else (confunion base-path override-path))))

;; Finally, this is the public entry point for this API. This function build a configuration
;; map reading EDN files, and validates it against a schema (which is also validated: it needs
;; to be well-formed).
(defn load-configuration
  "Builds a configuration map merging a base configuration map with an override one, reading
  both of them from the first existing file in the ordered sequences of choices.
  Both the schema and the resulting configuration are validated (the schema for well-formedness,
  and the configuration against that schema). Returns the configuration if everything is ok,
  otherwise it generates an exception with a complete description of the problems."
  [schema-path base-paths choices]
  (let [schema (schema/verify-schema (load-edn schema-path))
        conf (read-configuration base-paths choices)]
    (schema/verify-conf conf schema)))


;; However, we also want to support the composition of the schema too.

(defn read-schema
  "Compose a global schema by merging multiple schema files."
  [base-paths additional-paths]
  (let [base-path (first-existing base-paths)
        additional-path (first-existing additional-paths)]
    (cond (nil? base-path) (throw (Exception. "Base schema file not found!"))
          (nil? additional-path) (load-edn base-path)
          :else (schema/compose-schema base-path additional-path))))

(defn compose-configuration
  "Builds a configuration map by merging a base configuration map with
  an optional override one, and validating the final result against a
  schema. The schema is also build by compositing a base schema file
  and an ordered set of extension files. Both final data
  structures (configuration and schema) are validated by itself then
  against one another. If everything is good, the final configuration
  is returned, otherwise it generates an exception with a complete
  description of the problems."
  [schema-base-paths schema-choices base-paths choices]
  (let [schema (read-schema schema-base-paths schema-choices)
        conf (read-configuration base-paths choices)]
    (schema/verify-conf conf schema)))
