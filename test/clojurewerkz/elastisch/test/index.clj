(ns clojurewerkz.elastisch.test.index
  (:refer-clojure :exclude [get replace])
  (:require [clj-http.client         :as http]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as index]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.test.fixtures :as fx])
  (:use clojurewerkz.elastisch.document
        clojurewerkz.elastisch.response
        clojure.test))

(use-fixtures :each fx/delete-people-index)

;;
;; create, delete, exists?
;;

(deftest create-index-test
  (let [index    "people"
        response (index/create index :mappings fx/people-mapping)]
    (is (ok? response))
    (is (index/exists? index))))

(deftest delete-index-test
  (let [index    "people"
        _        (index/create index :mappings fx/people-mapping)
        response (index/delete index)]
    (is (ok? response))
    (is (not (index/exists? index)))))

;;
;; Mappings
;;

(deftest get-index-mapping-test
  (let [index    "people"
        mappings fx/people-mapping
        response (index/create index :mappings mappings)]
    (is (ok? response))
    (is (= mappings (:people (index/get-mapping index))))
    (is (= mappings (:people (index/get-mapping [index, "shmeople"]))))
    (is (= mappings (index/get-mapping index "person")))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fx/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" } } } })
        response (index/update-mapping index "person" :mapping mapping)]
    (is (= (ok? response)))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fx/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" :store "no" } } } })
        response (index/update-mapping index "person" :mapping mapping :ignore-conflicts false)]
    (is (= (ok? response)))))

(deftest create-index-mapping-test
  (let [index    "people"
        mapping  fx/people-mapping
        _        (index/create index :mappings {})
        response (index/update-mapping index "person" :mapping mapping)]
    (is (ok? response))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest delete-index-mapping-test
  (let [index       "people"
        index-type  "person"
        _           (index/create index :mappings fx/people-mapping)
        response    (index/delete-mapping index, index-type)]
    (is (ok? response))
    (is (nil? ((index/get-mapping index) index-type)))))

;;
;; Settings
;;

(deftest get-index-settings-test
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :settings settings :mappings fx/people-mapping)]
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

(deftest update-index-settings-global
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :mappings fx/people-mapping)
        response  (index/update-settings settings)]
    (is (ok? response))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

(deftest update-index-settings-concrete
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :mappings fx/people-mapping)
        response  (index/update-settings index settings)]
    (is (ok? response))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

;;
;; Open/close
;;

(deftest open-close-index-test
  (let [index     "people"
        _         (index/create index :mappings fx/people-mapping)]
    (is (ok? (index/open index)))
    (is (ok? (index/close index)))))
