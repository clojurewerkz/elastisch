(ns clojurewerkz.elastisch.native-api.indices-test
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.native        :as es]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.native.index  :as idx]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th])
  (:use clojure.test
        [clojurewerkz.elastisch.rest.response :only [acknowledged?]]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

(defn- broadcast-operation-response?
  [m]
  (and (:total-shards m)
       (:successful-shards m)
       (:failed-shards m)))


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
;; Open/close
;;

(deftest ^{:indexing true :native true} test-open-close-index
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (acknowledged? (idx/open index)))
    (is (acknowledged? (idx/close index)))))

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

(deftest ^{:indexing true :native true} test-flush-index-with-refresh
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/flush index :refresh true)]
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

(deftest ^{:indexing true :native true} test-index-status
     (let [index     "people"
           _         (idx/create index :mappings fx/people-mapping)
           response  (idx/status index :recovery true)]
       (is (broadcast-operation-response? response))))

(deftest ^{:indexing true :native true} test-index-status-for-multiple-indexes
     (idx/create "group1")
     (idx/create "group2")
     (is (broadcast-operation-response? (idx/status ["group1" "group2"] :recovery true :snapshot true))))


;;
;; Segments
;;

(deftest ^{:indexing true :native true} test-index-status
     (let [index     "people"
           _         (idx/create index :mappings fx/people-mapping)]
       (is (broadcast-operation-response? (idx/segments index)))))

(deftest ^{:indexing true :native true} test-index-status-for-multiple-indexes
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

#_ (deftest ^{:indexing true :native true} test-create-an-index-with-two-aliases
     (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
     (println (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                   {:add {:index "aliased-index" :alias "alias2"}}]))
     (is (= "42s" (get-in (idx/get-settings "alias2") [:aliased-index :settings :index.refresh_interval]))))


#_ (deftest ^{:indexing true :native true} test-getting-aliases
     (idx/create "aliased-index" :settings {"index" {"refresh_interval" "42s"}})
     (println (idx/update-aliases [{:add {:index "aliased-index" :alias "alias1"}}
                                   {:add {:index "aliased-index" :alias "alias2" :routing 1}}]))
     (is (= {:aliased-index {:aliases {:alias2 {} :alias1 {}}}}
            (idx/get-aliases "aliased-index"))))

;;
;; Templates
;;

#_ (deftest ^{:indexing true :native true} test-create-an-index-template-and-fetch-it
     (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
     (is (= {:accounts {:template "account*"
                        :order 0
                        :settings {:index.refresh_interval "60s"}
                        :mappings {}}}
            (idx/get-template "accounts"))))

#_ (deftest ^{:indexing true :native true} test-create-an-index-template-and-delete-it
     (idx/create-template "accounts" :template "account*" :settings {:index {:refresh_interval "60s"}})
     (println (idx/delete-template "accounts")))
