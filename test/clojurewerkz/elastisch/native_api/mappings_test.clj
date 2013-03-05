(ns clojurewerkz.elastisch.native-api.mappings-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.native.index :as idx]
            [clojurewerkz.elastisch.fixtures     :as fx]
            [clojurewerkz.elastisch.test.helpers :as th])
  (:use clojure.test
        clojurewerkz.elastisch.native.response))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

;;
;; Mappings
;;

(deftest ^{:native true} test-updating-index-mapping
  (let [index    "people1"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string"}}}})
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts true)]
    (is (= (ok? response)))))

(deftest ^{:native true} test-updating-index-mapping-ignoring-conflicts
  (let [index    "people2"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string" :store "no"}}}})
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts true)]
    (is (ok? response))))

(deftest ^{:native true} test-updating-blank-index-mapping
  (let [index    "people3"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (ok? response))))

(deftest ^{:native true} test-delete-index-mapping
  (let [index        "people4"
        mapping-type "person"
        _            (idx/create index :mappings fx/people-mapping)
        response     (idx/delete-mapping index mapping-type)]
    (is (ok? response))))
