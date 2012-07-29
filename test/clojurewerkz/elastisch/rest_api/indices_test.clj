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

(deftest test-successful-creation-of-index-with-mappings-and-without-settings
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (ok? response))
    (is (idx/exists? index))))

(deftest test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (ok? response))
    (is (not (idx/exists? index)))))

;;
;; Settings
;;

(deftest test-getting-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :settings settings :mappings fx/people-mapping)]
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest test-updating-global-index-settings
  (let [index     "people"
        settings  {:index {:refresh_interval "1s"}}
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest testing-updating-specific-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings index settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

;;
;; Open/close
;;

(deftest open-close-index-test
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/open index)))
    (is (ok? (idx/close index)))))
