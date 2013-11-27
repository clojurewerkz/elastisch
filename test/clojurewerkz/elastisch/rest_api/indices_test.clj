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

(deftest ^{:indexing true} test-create-an-index-without-mappings-or-settings
  (let [response (idx/create "elastisch-index-without-mappings")]
    (is (ok? response))
    (is (acknowledged? response))))

(deftest ^{:indexing true} test-create-an-index-with-settings
  (let [response (idx/create "elastisch-index-without-mappings" :settings {"index" {"number_of_shards" 1}})]
    (is (ok? response))
    (is (acknowledged? response))))

(deftest ^{:indexing true} test-successful-creation-of-index-with-mappings-and-without-settings
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (ok? response))
    (is (idx/exists? index))))

(deftest ^{:indexing true} test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (ok? response))
    (is (not (idx/exists? index)))))

;;
;; Settings
;;

(deftest ^{:indexing true} test-getting-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :settings settings :mappings fx/people-mapping)]
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest ^{:indexing true} test-updating-global-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest ^{:indexing true} testing-updating-specific-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings index settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

;;
;; Optimize
;;

(deftest ^{:indexing true} test-optimize-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/optimize index :only_expunge_deletes 1)))))

;;
;; Flush
;;

(deftest ^{:indexing true} test-flush-index-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/flush index :refresh true)))))

;;
;; Snapshot
;;

(deftest ^{:indexing true} test-snapshot-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/snapshot index)))))

;;
;; Clear cache
;;

(deftest ^{:indexing true} test-clear-index-cache-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/clear-cache index :filter true :field_data true)))))


;;
;; Status
;;

(deftest ^{:indexing true} test-index-status
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/status index :recovery true)))))

(deftest ^{:indexing true} test-index-status-for-multiple-indexes
  (idx/create "group1")
  (idx/create "group2")
  (is (ok? (idx/status ["group1" "group2"] :recovery true :snapshot true))))


;;
;; Segments
;;

(deftest ^{:indexing true} test-index-status
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/segments index)))))

(deftest ^{:indexing true} test-index-status-for-multiple-indexes
  (idx/create "group1")
  (idx/create "group2")
  (is (ok? (idx/segments ["group1" "group2"]))))


;;
;; Stats
;;

(deftest ^{:indexing true} test-index-stats
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/stats index :docs true :store true :indexing true)))))

(deftest ^{:indexing true} test-index-stats-for-multiple-indexes
  (idx/create "group1")
  (idx/create "group2")
  (is (ok? (idx/stats ["group1" "group2"] :docs true :store true :indexing true))))


;;
;; Aliases
;;

(deftest ^{:indexing true} test-create-an-index-with-two-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (is (ok? (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                {:add {:index "aliased-index" :alias "alias2"}}])))
  (is (= "42s" (get-in (idx/get-settings "alias2") [:aliased-index :settings :index.refresh_interval]))))


(deftest ^{:indexing true} test-getting-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (is (ok? (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                {:add {:index "aliased-index" :alias "alias2" :routing 1}}])))
  (is (= {:aliased-index {:aliases {:alias2 {} :alias1 {}}}}
         (idx/get-aliases "aliased-index"))))

;;
;; Templates
;;

(deftest ^{:indexing true} test-create-an-index-template-and-fetch-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (is (= {:accounts {:template "account*"
                     :order 0
                     :settings {:index.refresh_interval "60s"}
                     :mappings {}}}
         (idx/get-template "accounts"))))

(deftest ^{:indexing true} test-create-an-index-template-and-delete-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (is (ok? (idx/delete-template "accounts"))))
