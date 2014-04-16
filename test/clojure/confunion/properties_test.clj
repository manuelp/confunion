(ns confunion.properties_test
  (:require [clojure.test :refer :all]
            [confunion.properties :refer :all])
  (:import java.util.Properties))

(deftest propertize-map-test
  (testing "should convert to strings keys and values of a map"
    (let [m {:a 42
             :b [1 2 3]
             :c {:prop "val"}}]
      (is (= {"a" "42"
              "b" "[1 2 3]"
              "c" "{\"prop\" \"val\"}"}
             (propertize-map m))))))

(deftest add-properties-test
  (testing "should populate a java.util.Properties object with an EDN map"
    (let [props (new java.util.Properties)
          m {:a 1}]
      (add-properties props m)
      (is (= "1" (.getProperty props "a")))))
  (testing "pre-existing properties should be overwritten"
    (let [props (doto (new java.util.Properties)
                  (.setProperty "a" "12"))
          m {:a 1}]
      (add-properties props m)
      (is (= "1" (.getProperty props "a"))))))

#_(run-tests)
