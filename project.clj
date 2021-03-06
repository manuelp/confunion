(defproject me.manuelp/confunion "0.3.0"
  :description "Library for managing configuration based on EDN files."
  :url "http://github.com/manuelp/confunion"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-marginalia "0.9.0"]]

  :source-paths ["src/clojure"]
  :test-paths ["test/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.7"
                  "-source" "1.7"]

  :signing {:gpg-key "077AB1AC"})
