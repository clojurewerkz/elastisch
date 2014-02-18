;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.indices-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.native        :as es]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.native.index  :as idx]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th]
            [clojurewerkz.elastisch.native.response :refer [acknowledged?]]
            [clojure.test :refer :all]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

(defn- broadcast-operation-response?
  [m]
  (and (get-in m [:_shards :total])
       (get-in m [:_shards :successful])
       (get-in m [:_shards :failed])))


;;
;; create, delete, exists?
;;

(deftest ^{:indexing true :native true} test-create-an-index-without-mappings-or-settings
  (let [response (idx/create "elastisch-index-without-mappings")]
    (is (acknowledged? response))))

(deftest ^{:indexing true :native true} test-create-an-index-with-settings
  (let [response (idx/create "elastisch-index-without-mappings" :settings {"index" {"number_of_shards" 1}})]
    (is (acknowledged? response))))

(deftest ^{:indexing true :native true} test-successful-creation-of-index-with-mappings-and-without-settings
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (idx/exists? index))))

(deftest ^{:indexing true :native true} test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (not (idx/exists? index)))))

;;
;; Settings
;;

(deftest ^{:indexing true :native true} testing-updating-specific-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :mappings fx/people-mapping)]
    (is (idx/update-settings index settings))))

;;
;; Optimize
;;

(deftest ^{:indexing true :native true} test-optimize-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/optimize index :only_expunge_deletes 1)]
    (is (broadcast-operation-response? response))))

;;
;; Flush
;;

(deftest ^{:indexing true :native true} test-flush-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/flush index)]
    (is (broadcast-operation-response? response))))


;;
;; Refresh
;;

(deftest ^{:indexing true :native true} test-refresh-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/refresh index)]
    (is (broadcast-operation-response? response))))


;;
;; Snapshot
;;

(deftest ^{:indexing true :native true} test-snapshot-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/snapshot index)]
    (is (broadcast-operation-response? response))))

;;
;; Clear cache
;;

(deftest ^{:indexing true :native true} test-clear-index-cache-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/clear-cache index :filter true :field_data true)]
    (is (broadcast-operation-response? response))))


;;
;; Status
;;

(deftest ^{:indexing true :native true} test-index-status-1
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/status index :recovery true)]
    (is (broadcast-operation-response? response))))

(deftest ^{:indexing true :native true} test-index-status-for-multiple-indexes-1
  (idx/create "group1")
  (idx/create "group2")
  (is (broadcast-operation-response? (idx/status ["group1" "group2"] :recovery true :snapshot true))))


;;
;; Segments
;;

(deftest ^{:indexing true :native true} test-index-status-2
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (broadcast-operation-response? (idx/segments index)))))

(deftest ^{:indexing true :native true} test-index-status-for-multiple-indexes-2
  (idx/create "group1")
  (idx/create "group2")
  (is (broadcast-operation-response? (idx/segments ["group1" "group2"]))))


;;
;; Stats
;;

(deftest ^{:indexing true :native true} test-index-stats-for-all-indexes
  (idx/create "group1")
  (idx/create "group2")
  (is (idx/stats :docs true :store true :indexing true)))


;;
;; Aliases
;;

(deftest ^{:indexing true :native true} test-create-an-index-with-two-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (is (acknowledged? (idx/update-aliases [{:add {:alias "alias1" :indices ["aliased-index"]}}
                                          {:add {:alias "alias2" :indices ["aliased-index"]}}]))))

;;
;; Templates
;;

(deftest ^{:indexing true :native true} test-create-an-index-template-and-fetch-it
  (let [response (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})]
    (is (acknowledged? response))))

(deftest ^{:indexing true :native true} test-create-an-index-template-and-delete-it
   (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
   (is (acknowledged? (idx/delete-template "accounts"))))
