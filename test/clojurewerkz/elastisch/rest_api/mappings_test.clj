(ns clojurewerkz.elastisch.rest-api.indices-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.rest       :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.fixtures   :as fx])
  (:use clojurewerkz.elastisch.rest.document
        clojurewerkz.elastisch.rest.response
        clojure.test))

(use-fixtures :each fx/reset-indexes)

;;
;; Mappings
;;

(deftest test-getting-index-mapping
  (let [index    "people"
        mappings fx/people-mapping
        response (idx/create index :mappings mappings)]
    (is (ok? response))
    (is (= mappings (:people (idx/get-mapping index))))
    (is (= mappings (:people (idx/get-mapping [index, "shmeople"]))))
    (is (= mappings (idx/get-mapping index "person")))))

(deftest test-updating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string"}}}})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (= (ok? response)))
    (is (= mapping (:people (idx/get-mapping index))))))

(deftest test-updating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string" :store "no"}}}})
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts false)]
    (is (= (ok? response)))))

(deftest test-creating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (ok? response))
    (is (= mapping (:people (idx/get-mapping index))))))

(deftest test-delete-index-mapping
  (let [index        "people"
        mapping-type "person"
        _            (idx/create index :mappings fx/people-mapping)
        response     (idx/delete-mapping index mapping-type)]
    (is (ok? response))
    (is (nil? ((idx/get-mapping index) mapping-type)))))
