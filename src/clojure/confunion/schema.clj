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
;; * `:schema/type`: a keyword that describes the type of the parameter value. Currently only scalar values are supported:
;;   * :schema.type/string
;;   * :schema.type/boolean
;;   * :schema.type/number
;;   * :schema.type/any (this is a catch-all, to use *only* if none of the existing keys describe the value type)

;; Parameter value types are tied to Java classes: values, read from EDN files as Java object instances, should be of the specified type. This way we can track, verify and migrate (for example from strings to booleans or vice-versa) configuration values.
(def value-types {:schema.type/string java.lang.String
                  :schema.type/number java.lang.Number
                  :schema.type/boolean java.lang.Boolean
                  :schema.type/any java.lang.Object})

;; All this *parameter description* parameters are mandatory.
;;
;; An example:

(comment [{:schema/param :a
           :schema/doc "A very useful configuration"
           :schema/mandatory false
           :schema/type :schema.type/string}
          {:schema/param :b
           :schema/doc "Some other useful configuration parameter."
           :schema/mandatory true
           :schema/type :schema.type/boolean}])

(defn- check-schema-param
  "Checks if it's present the given keyword (schema entry) in the parameter description."
  [param-desc schema-entry]
  (if (contains? param-desc schema-entry)
    :ok
    {:error :missing-schema-entry
     :entry schema-entry
     :param param-desc}))

(defn- check-param-value
  "Check if the value associated to the schema-entry key is among the possible values."
  [param-desc schema-entry possible-values]
  (when (contains? param-desc schema-entry)
    (if (contains? possible-values (schema-entry param-desc))
      :ok
      {:error :wrong-value-type
       :entry schema-entry
       :param param-desc})))

(defn- validate-param-desc
  "Checks if the parameter description contains all the required entries and with the right values.
  Returns a collection of errors (can be empty)."
  [param-desc]
  (filter #(and (not (nil? %)) (not= :ok %))
          (vector (check-schema-param param-desc :schema/param)
                  (check-schema-param param-desc :schema/doc)
                  (check-schema-param param-desc :schema/mandatory)
                  (check-schema-param param-desc :schema/type)
                  (check-param-value param-desc
                                     :schema/type
                                     #{:schema.type/string
                                       :schema.type/boolean
                                       :schema.type/number
                                       :schema.type/any}))))

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
  Returns `:ok` if the map contains that parameter
  or an error with the parameter description otherwise."
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

(defn- right-type?
  "Verifies if the configuration parameter specified by `param` is
   present in the map `m` and of the right type."
  [m param]
  (let [pname (:schema/param param)
        ptype (:schema/type param)
        pvalue (get m pname :not-present)]
    (cond (contains? m pname) (instance? (ptype value-types) pvalue)
          :else true)))

(defn get-param-value
  "Takes the value in the configuration map `9` corresponding to the
   parameter described by `param`."
  [m param]
  (get m (:schema/param param)))

(defn-
  "Checks if the parameter described by the *parameter description* `param`,
   if present in the configuration map `m`, is of the right type.
   Returns `:ok` if the parameter is present in `m` and of the right type,
   otherwise an error map is created with the type error details.

   Note: it assumes that checks about presence of mandatory parameters is
   done by other functions."
  check-type
  [m param]
  (cond (not (right-type? m param)) {:error :type-error
                                     :param param
                                     :expected (:schema/type param)
                                     :actual (class (get-param-value m param))}
        :else :ok))

(defn validate-conf
  "Validate a map `conf` against a `schema` definition, and returns a seq
  of errors (may be empty if the map is fine according to the schema)."
  [conf schema]
  (let [s1 (map (partial check-param conf) schema)
        s2 (map (partial check-type conf) schema)
        s3 (map (partial check-description schema) conf)]
    (filter #(not= :ok %) (concat s1 s2 s3))))

(defn verify-conf
  "Validate a configuration map against a schema, and returns the configuration
  if it's valid. Otherwise it generates an exception with an informative message."
  [conf schema]
  (let [errors (validate-conf conf schema)]
    (if (empty? errors)
      conf
      (generate-exception "Invalid configuration" errors))))
