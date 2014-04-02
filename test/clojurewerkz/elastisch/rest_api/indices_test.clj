;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.indices-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.rest       :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.fixtures   :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojurewerkz.elastisch.rest.document :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

;;
;; create, delete, exists?
;;

(deftest ^{:rest true :indexing true} test-create-an-index-without-mappings-or-settings
  (let [response (idx/create "elastisch-index-without-mappings")]
    (is (acknowledged? response))
    (is (not (idx/type-exists? "elastisch-index-without-mappings" "person")))))

(deftest ^{:rest true :indexing true} test-create-an-index-with-settings
  (let [response (idx/create "elastisch-index-without-mappings" :settings {"index" {"number_of_shards" 1}})]
    (is (acknowledged? response))))

(deftest ^{:rest true :indexing true} test-successful-creation-of-index-with-mappings-and-without-settings
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (idx/exists? index))
    (is (idx/type-exists? index "person"))))

(deftest ^{:rest true :indexing true} test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (not (idx/exists? index)))
    (is (not (idx/type-exists? index "person")))))

;;
;; Settings
;;

(deftest ^{:rest true :indexing true} test-getting-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        response  (idx/create index :settings settings :mappings fx/people-mapping)
        settings' (idx/get-settings "people")]
    (acknowledged? response)
    (is (= "1s" (get-in settings' [:people :settings :index :refresh_interval])))))

(deftest ^{:rest true :indexing true} testing-updating-specific-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "7s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings index settings)
        settings' (idx/get-settings "people")]
    (acknowledged? response)
    (is (= "7s" (get-in settings' [:people :settings :index :refresh_interval])))))

;;
;; Optimize
;;

(deftest ^{:rest true :indexing true} test-optimize-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/optimize index :only_expunge_deletes 1)]
    (is (:_shards response))))

;;
;; Flush
;;

(deftest ^{:rest true :indexing true} test-flush-index-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/flush index :refresh true)]
    (is (:_shards response))))

;;
;; Snapshot
;;

(deftest ^{:rest true :indexing true} test-snapshot-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/snapshot index)]
    (is (:_shards response))))

;;
;; Clear cache
;;

(deftest ^{:rest true :indexing true} test-clear-index-cache-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/clear-cache index :filter true :field_data true)]
    (is (:_shards response))))


;;
;; Status
;;

(deftest ^{:rest true :indexing true} test-index-status-1
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/status index :recovery true)]
    (is (:_shards response))))

(deftest ^{:rest true :indexing true} test-index-status-for-multiple-indexes-1
  (idx/create "group1")
  (idx/create "group2")
  (let [response (idx/status ["group1" "group2"] :recovery true :snapshot true)]
    (is (:_shards response))))


;;
;; Segments
;;

(deftest ^{:rest true :indexing true} test-index-status-2
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/segments index)]
    (is (:_shards response))))

(deftest ^{:rest true :indexing true} test-index-status-for-multiple-indexes-2
  (idx/create "group1")
  (idx/create "group2")
  (let [response (idx/segments ["group1" "group2"])]
    (is (:_shards response))))


;;
;; Stats
;;

(deftest ^{:rest true :indexing true} test-index-stats
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/stats index :docs true :store true :indexing true)]
    (is (:_shards response))))

(deftest ^{:rest true :indexing true} test-index-stats-for-multiple-indexes
  (idx/create "group1")
  (idx/create "group2")
  (let [response (idx/stats ["group1" "group2"] :docs true :store true :indexing true)]
    (is (:_shards response))))


;;
;; Aliases
;;

(deftest ^{:rest true :indexing true} test-create-an-index-with-two-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (let [response (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                      {:add {:index "aliased-index" :alias "alias2"}}])]
    (acknowledged? response))
  (is (= "42s" (get-in (idx/get-settings "alias2")
                       [:aliased-index :settings :index :refresh_interval]))))


(deftest ^{:rest true :indexing true} test-getting-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (let [res1 (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                      {:add {:index "aliased-index" :alias "alias2" :routing 1}}])
        res2 (idx/get-aliases "aliased-index")]
    (acknowledged? res1)
    (is (get-in res2 [:aliased-index :aliases]))))

;;
;; Templates
;;

(deftest ^{:rest true :indexing true} test-create-an-index-template-and-fetch-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (let [res (idx/get-template "accounts")]
    (is (= "account*" (get-in res [:accounts :template])))
    (is (get-in res [:accounts :settings]))))

(deftest ^{:rest true :indexing true} test-create-an-index-template-and-delete-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (acknowledged? (idx/delete-template "accounts")))
