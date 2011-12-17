(ns elastisch.test.core
  (:require [elastisch.urls          :as urls])
  (:use [elastisch.core]
        [clojure.test]))

(deftest index-mapping-url-test
  (is (= "http://localhost:9200/index_name" (urls/index-mapping "index_name")))
  (is (= "http://localhost:9200/index_name/type_name" (urls/index-mapping "index_name" "type_name")))
  (is (= "http://localhost:9200/index_name/type_name?ignore_conflicts=false" (urls/index-mapping "index_name" "type_name" false))))