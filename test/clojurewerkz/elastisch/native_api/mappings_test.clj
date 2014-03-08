;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.mappings-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.native.index :as idx]
            [clojurewerkz.elastisch.fixtures     :as fx]
            [clojurewerkz.elastisch.test.helpers :as th]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

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
    (is (created-or-acknowledged? response))
    (is (get-in (idx/get-mapping index) [:people1 :mappings :person :properties :username :store]))))

(deftest ^{:native true} test-updating-index-mapping-ignoring-conflicts
  (let [index    "people2"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {:person {:properties {:first-name {:type "string" :store "no"}}}})
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts true)]
    (is (created-or-acknowledged? response))))

(deftest ^{:native true} test-updating-blank-index-mapping
  (let [index    "people3"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (created-or-acknowledged? response))))

(deftest ^{:native true} test-delete-index-mapping
  (let [index        "people4"
        mapping-type "person"
        _            (idx/create index :mappings fx/people-mapping)
        response     (idx/delete-mapping index mapping-type)]
    (is (created-or-acknowledged? response))))
