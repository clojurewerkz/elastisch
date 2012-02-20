(ns clojurewerkz.elastisch.test.rest
  (:require [clojurewerkz.elastisch.rest :as rest])
  (:use [clojure.test]))

(deftest index-mapping-url-test
  (are [expected actual] (= expected actual)
       "http://localhost:9200/index_name/_mapping"
       (rest/index-mapping "index_name")

       "http://localhost:9200/index_name/type_name/_mapping"
       (rest/index-mapping "index_name" "type_name")

       "http://localhost:9200/index_name/type_name/_mapping?ignore_conflicts=false"
       (rest/index-mapping "index_name" "type_name" false)))

(deftest record-url-test
  (is (= "http://localhost:9200/index_name/type_name/1?op_type=3&version=2"
         (rest/record "index_name" "type_name" 1 { :version 2 :op_type 3 }))))


(deftest index-mget-test
  (is (= "http://localhost:9200/_mget"
         (rest/index-mget)))
  (is (= "http://localhost:9200/index_name/_mget"
         (rest/index-mget "index_name")))
  (is (= "http://localhost:9200/index_name/type_name/_mget"
         (rest/index-mget "index_name" "type_name"))))


(deftest connect-test
  (is (= "http://localhost:9200" (:uri rest/*endpoint*)))
  (is (not (nil? (:version rest/*endpoint*)))))