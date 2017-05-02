(ns confunion.schema-test
  (:require [clojure.test :refer :all]
            [confunion.schema :refer :all]))

(deftest schema-validations
  (testing "a schema should be a vector"
    (is (= [] (validate-schema [])))
    (is (= [{:error :invalid-schema
             :message "Should be a vector!"}]
           (validate-schema {:not "valid"}))))
  (testing "a schema should contain complete parameter description maps"
    (is (= [{:error :missing-schema-entry
             :entry :schema/param
             :param {}}
            {:error :missing-schema-entry
             :entry :schema/doc
             :param {}}
            {:error :missing-schema-entry
             :entry :schema/mandatory
             :param {}}
            {:error :missing-schema-entry
             :entry :schema/type
             :param {}}]
           (validate-schema [{}])))
    (is (= [{:error :missing-schema-entry
             :entry :schema/mandatory
             :param {:schema/param :a
                     :schema/doc "docstring"}}
            {:error :missing-schema-entry
             :entry :schema/type
             :param {:schema/param :a
                     :schema/doc "docstring"}}]
           (validate-schema [{:schema/param :a
                              :schema/doc "docstring"}])))
    (is (= []
           (validate-schema [{:schema/param :a
                              :schema/doc "docstring"
                              :schema/mandatory true
                              :schema/type :schema.type/string}]))))
  (testing "failed validations should produce a detailed exception"
    (is (= [{:schema/param :a
             :schema/doc "docstring"
             :schema/mandatory true
             :schema/type :schema.type/string}]
           (verify-schema [{:schema/param :a
                            :schema/doc "docstring"
                            :schema/mandatory true
                            :schema/type :schema.type/string}])))
    (is (thrown-with-msg? Exception #"Invalid schema definition:\n- Invalid schema:.*"
                          (verify-schema {})))
    (is (thrown-with-msg? Exception #"Invalid schema definition:\n- Schema parameter \{:schema/param :a.*is missing in the entry.*"
                          (verify-schema [{:schema/param :a
                                           :schema/doc "docstring"}])))))

(defn schema-param
  ([k mandatory] {:schema/param k
                  :schema/doc "..."
                  :schema/mandatory mandatory
                  :schema/type :schema.type/string})
  ([k mandatory type] {:schema/param k
                       :schema/doc "..."
                       :schema/mandatory mandatory
                       :schema/type type}))

(deftest configuration-validations
  (testing "configuration should be valid according to a schema"
    (is (= [] (validate-conf {} [])))
    (is (= [] (validate-conf {:a 42}
                             [{:schema/param :a
                               :schema/doc "docstring"
                               :schema/mandatory true
                               :schema/type :schema.type/number}]))))
  (testing "configuration should not contain only properties described in the schema"
    (is (= [{:error :unknown
             :param [:b "unknown"]}]
           (validate-conf {:b "unknown"} [(schema-param :a false)]))))
  (testing "configuration should contain all mandatory parameters described in the schema"
    (is (= [{:error :missing
             :param (schema-param :a true)}]
           (validate-conf {} [(schema-param :a true)])))
    (is (= []
           (validate-conf {} [(schema-param :a false)]))))
  (testing "verification should generate an informative exception when the configuration is invalid"
    (is (= {:b 2}
           (verify-conf {:b 2}
                        [(schema-param :b true :schema.type/number)])))
    (is (thrown-with-msg? Exception #"Invalid configuration:\n- Missing mandatory parameter required by the schema: \(:b"
                          (verify-conf {}
                                       [(schema-param :b true)]))))
  (testing "parameter values should be checked by type"
    (is (= [{:error :type-error
             :param (schema-param :a true :schema.type/string)
             :expected :schema.type/string
             :actual java.lang.Boolean}]
           (validate-conf {:a false}
                          [(schema-param :a true :schema.type/string)])))
    (is (= [{:error :type-error
             :param (schema-param :a true :schema.type/boolean)
             :expected :schema.type/boolean
             :actual java.lang.String}]
           (validate-conf {:a "false"}
                          [(schema-param :a true :schema.type/boolean)])))))

#_(run-tests)
