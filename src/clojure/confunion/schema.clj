(ns confunion.schema
  "Configuration files can be checked against a schema that defines the
  *shape* the configuration will have."
  (:require [confunion.edn :refer [load-edn]]))

;; ## Schemas
;;
;; A schema is a simple data structure that describes what parameters
;; a configuration map should contain.
;;
;; A schema is a vector of maps, each one of them describes a configuration parameter with some keys:
;;
;; * `:schema/param`: the keyword of the parameter (it's name, or code if you want).
;; * `:schema/doc`: a documentation string (which is useful both for documentation and useful error messages).
;; * `:schema/mandatory`: a boolean that indicates if the described parameter is mandatory or not.
;;
;; All this *parameter description* parameters are mandatory.
;;
;; An example:

(comment [{:schema/param :a
           :schema/doc "A very useful configuration"
           :schema/mandatory false}
          {:schema/param :b
           :schema/doc "Some other useful configuration parameter."
           :schema/mandatory true}])

(defn- check-schema-param
  "Checks if it's present the given keyword (schema entry) in the parameter description."
  [param-desc schema-entry]
  (if (contains? param-desc schema-entry)
    :ok
    {:error :missing-schema-entry
     :entry schema-entry
     :param param-desc}))

(defn- validate-param-desc
  "Checks if the parameter description contains all the required entries.
  Returns a collection of errors (can be empty)."
  [param-desc]
  (filter #(not= :ok %)
          (vector (check-schema-param param-desc :schema/param)
                  (check-schema-param param-desc :schema/doc)
                  (check-schema-param param-desc :schema/mandatory))))

(defn validate-schema
  "Validates a configuration schema, checking if it's a vector and
  contains only valid parameter descriptions."
  [schema]
  (if (not (vector? schema))
    [{:error :invalid-schema
      :message "Should be a vector!"}]
    (apply concat (filter not-empty (map validate-param-desc schema)))))

(defn- generate-exception [summary errors]
  (let [details (clojure.string/join \newline (map str errors))
        description (str summary ":" \newline details)]
    (throw (Exception. description))))

(defn verify-schema
  "Verify if a schema is correctly formed. Returns the schema itself if it's well
  formed, otherwise generates an exception with a detailed message."
  [schema]
  (let [schema-errors (validate-schema schema)]
    (if (empty? schema-errors)
      schema
      (generate-exception "Invalid schema definition" schema-errors))))

;; ## Configuration Validation
;;
;; When we're sure that a schema is valid (it has the right shape), we can use
;; it to validate a configuration.

(defn- check-param
  "Checks the map `m` against a parameter description.
  Returns `:ok` if the map contains that parameter, or an error with
  the parameter description otherwise."
  [m param]
  (letfn [(missing-param? []
                          (and (not (contains? m (:schema/param param)))
                               (:schema/mandatory param)))]
    (cond (missing-param?) {:error :missing
                            :param param}
          :else :ok)))

(defn- check-description
  "Check a configuration map entry agains a schema. Returns `:ok` if the entry is
  documented in the schema, or an error with the offending entry otherwise."
  [schema entry]
  (letfn [(describes? [param param-desc]
                      (= param (:schema/param param-desc)))]
    (if (not-empty (filter (partial describes? (key entry)) schema))
      :ok
      {:error :unknown
       :param entry})))

(defn validate-conf
  "Validate a map `conf` against a `schema` definition, and returns a seq
  of errors (may be empty if the map is fine according to the schema)."
  [conf schema]
  (let [s1 (map (partial check-param conf) schema)
        s2 (map (partial check-description schema) conf)]
    (filter #(not= :ok %) (concat s1 s2))))

(defn verify-conf
  "Validate a configuration map against a schema, and returns the configuration
  if it's valid. Otherwise it generates an exception with an informative message."
  [conf schema]
  (let [errors (validate-conf conf schema)]
    (if (empty? errors)
      conf
      (generate-exception "Invalid configuration" errors))))
