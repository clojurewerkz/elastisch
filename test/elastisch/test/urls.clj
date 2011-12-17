(ns elastisch.test.urls
  (:require [elastisch.urls          :as urls])
  (:use [elastisch.core]
        [clojure.test]))

(deftest index-mapping-url-test
  (are [expected actual] (= expected actual)
       "http://localhost:9200/index_name/_mapping"
       (urls/index-mapping "index_name")

       "http://localhost:9200/index_name/type_name/_mapping"
       (urls/index-mapping "index_name" "type_name")

       "http://localhost:9200/index_name/type_name/_mapping?ignore_conflicts=false"
       (urls/index-mapping "index_name" "type_name" false)))

(deftest record-url-test
  (is (= "http://localhost:9200/index_name/type_name/1?op_type=3&version=2"
         (urls/record "index_name" "type_name" 1 :version 2 :op_type 3))))
