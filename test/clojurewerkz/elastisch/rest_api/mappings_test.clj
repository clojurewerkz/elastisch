(ns clojurewerkz.elastisch.rest-api.mappings-test
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

(deftest ^{:rest true} test-getting-index-mapping
  (let [index    "people1"
        mappings fx/people-mapping
        response (idx/create index :mappings mappings)]
    (is (ok? response))
    (is (-> (idx/get-mapping index) :people1 :person :properties :username :store))))

(deftest ^{:rest true} test-updating-index-mapping
  (let [index    "people2"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string"}}}})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (ok? response))))

(deftest ^{:rest true} test-updating-index-mapping-ignoring-conflicts
  (let [index    "people3"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string" :store "no"}}}})
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts true)]
    (is (ok? response))))

(deftest ^{:rest true} test-updating-blank-index-mapping
  (let [index    "people4"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (ok? response))
    (is (-> (idx/get-mapping index) :people4 :person :properties :username :store))))

(deftest ^{:rest true} test-delete-index-mapping
  (let [index        "people5"
        mapping-type "person"
        _            (idx/create index :mappings fx/people-mapping)
        response     (idx/delete-mapping index mapping-type)]
    (is (ok? response))
    (is (nil? ((idx/get-mapping index) mapping-type)))))
