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
    (is (created? response))
    (is (acknowledged? response))))

(deftest ^{:rest true :indexing true} test-create-an-index-with-settings
  (let [response (idx/create "elastisch-index-without-mappings" :settings {"index" {"number_of_shards" 1}})]
    (is (created? response))
    (is (acknowledged? response))))

(deftest ^{:rest true :indexing true} test-successful-creation-of-index-with-mappings-and-without-settings
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (created? response))
    (is (idx/exists? index))))

(deftest ^{:rest true :indexing true} test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (created? response))
    (is (not (idx/exists? index)))))

;;
;; Settings
;;

(deftest ^{:rest true :indexing true} test-getting-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :settings settings :mappings fx/people-mapping)]
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest ^{:rest true :indexing true} test-updating-global-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings settings)]
    (is (created? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest ^{:rest true :indexing true} testing-updating-specific-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings index settings)]
    (is (created? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

;;
;; Optimize
;;

(deftest ^{:rest true :indexing true} test-optimize-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/optimize index :only_expunge_deletes 1)))))

;;
;; Flush
;;

(deftest ^{:rest true :indexing true} test-flush-index-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/flush index :refresh true)))))

;;
;; Snapshot
;;

(deftest ^{:rest true :indexing true} test-snapshot-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/snapshot index)))))

;;
;; Clear cache
;;

(deftest ^{:rest true :indexing true} test-clear-index-cache-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/clear-cache index :filter true :field_data true)))))


;;
;; Status
;;

(deftest ^{:rest true :indexing true} test-index-status-1
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/status index :recovery true)))))

(deftest ^{:rest true :indexing true} test-index-status-for-multiple-indexes-1
  (idx/create "group1")
  (idx/create "group2")
  (is (created? (idx/status ["group1" "group2"] :recovery true :snapshot true))))


;;
;; Segments
;;

(deftest ^{:rest true :indexing true} test-index-status-2
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/segments index)))))

(deftest ^{:rest true :indexing true} test-index-status-for-multiple-indexes-2
  (idx/create "group1")
  (idx/create "group2")
  (is (created? (idx/segments ["group1" "group2"]))))


;;
;; Stats
;;

(deftest ^{:rest true :indexing true} test-index-stats
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (created? (idx/stats index :docs true :store true :indexing true)))))

(deftest ^{:rest true :indexing true} test-index-stats-for-multiple-indexes
  (idx/create "group1")
  (idx/create "group2")
  (is (created? (idx/stats ["group1" "group2"] :docs true :store true :indexing true))))


;;
;; Aliases
;;

(deftest ^{:rest true :indexing true} test-create-an-index-with-two-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (is (created? (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                {:add {:index "aliased-index" :alias "alias2"}}])))
  (is (= "42s" (get-in (idx/get-settings "alias2") [:aliased-index :settings :index.refresh_interval]))))


(deftest ^{:rest true :indexing true} test-getting-aliases
  (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
  (is (created? (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                {:add {:index "aliased-index" :alias "alias2" :routing 1}}])))
  (is (= {:aliased-index {:aliases {:alias2 {} :alias1 {}}}}
         (idx/get-aliases "aliased-index"))))

;;
;; Templates
;;

(deftest ^{:rest true :indexing true} test-create-an-index-template-and-fetch-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (is (= {:accounts {:template "account*"
                     :order 0
                     :settings {:index.refresh_interval "60s"}
                     :mappings {}}}
         (idx/get-template "accounts"))))

(deftest ^{:rest true :indexing true} test-create-an-index-template-and-delete-it
  (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
  (is (created? (idx/delete-template "accounts"))))
