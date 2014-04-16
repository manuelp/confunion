(ns confunion.core_test
  (:require [clojure.test :refer :all]
            [confunion.core :refer :all])
  #_(:import java.io.FileNotFoundException))

(defn a-b-files-fixture [f]
  (let [file-a (java.io.File. "a.edn")
        file-b (java.io.File. "b.edn")
        file-schema (java.io.File. "conf-schema.edn")]
    (try
      (spit file-a (pr-str {:a 1}))
      (spit file-b (pr-str {:a 2
                            :b "hello"}))
      (spit file-schema (pr-str [{:schema/param :a
                                  :schema/doc "docstring"
                                  :schema/mandatory true}]))
      (f)
      (finally
       (do
         (.delete file-a)
         (.delete file-b)
         (.delete file-schema))))))

(use-fixtures :once a-b-files-fixture)

(deftest confunion-test
  (testing "should merge multiple EDN maps read from files"
    (is (= {:a 2
            :b "hello"}
           (confunion "a.edn" "b.edn")))))

(deftest first-existing-test
  (testing "should return the path of the first existing file"
    (is (= nil (first-existing ["uknown-a.edn" "unknown-b.edn"])))
    (is (= "b.edn" (first-existing ["uknown-a.edn" "b.edn"])))
    (is (= "a.edn" (first-existing ["a.edn" "b.edn"])))))

(deftest read-configuration-test
  (testing "should return the base conf if there are no everrides"
    (is (= {:a 1}
           (read-configuration ["a.edn"] [])))
    (is (= {:a 1}
           (read-configuration ["a.edn"] ["uknown.edn" "another-unknown.edn"]))))
  (testing "should merge the base conf with the first override file found"
    (is (= {:a 2
            :b "hello"}
           (read-configuration ["a.edn"] ["unknown-b.edn" "b.edn"])))))

(deftest load-configuration-test
  (testing "the schema and the base configuration files should exist"
    (is (thrown-with-msg? java.io.FileNotFoundException #"unknown-schema.edn"
                          (load-configuration "unknown-schema.edn" ["a.edn"] [])))
    (is (thrown-with-msg? Exception #"Base configuration file not found!"
                          (load-configuration "conf-schema.edn" ["not.edn"] []))))
  (testing "the configuration should be validated against the schema"
    (is (thrown-with-msg? Exception #"Invalid configuration"
                          (load-configuration "conf-schema.edn" ["not.edn" "a.edn"] ["b.edn"])))
    (is (= {:a 1} (load-configuration "conf-schema.edn" ["a.edn"] [])))))

#_(run-tests)
