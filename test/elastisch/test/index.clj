(ns elastisch.test.index
  (:refer-clojure :exclude [get])
  (:require [clj-http.client         :as http]
            [elastisch.rest          :as rest]
            [elastisch.index         :as index]
            [elastisch.utils         :as utils]
            [elastisch.test.fixtures :as fixtures])
  (:use [elastisch.core]
        [clojure.test]))

(use-fixtures :each fixtures/delete-people-index)

;;
;; create, delete, exists?
;;

(deftest create-index-test
  (let [index    "people"
        response (index/create index :mappings fixtures/people-mapping)]
    (is (= true (utils/ok? response)))
    (is (= true (index/exists? index)))))

(deftest delete-index-test
  (let [index    "people"
        _        (index/create index :mappings fixtures/people-mapping)
        response (index/delete index)]
    (is (= true (utils/ok? response)))
    (is (= false (index/exists? index)))))

;;
;; Mappings
;;

(deftest get-index-mapping-test
  (let [index    "people"
        mappings fixtures/people-mapping
        response (index/create index :mappings mappings)]
    (is (= true (utils/ok? response)))
    (is (= mappings (:people (index/get-mapping index))))
    (is (= mappings (:people (index/get-mapping [index, "shmeople"]))))
    (is (= mappings (index/get-mapping index "person")))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" } } } })
        response (index/update-mapping index "person" :mapping mapping)]
    (is (= (utils/ok? response)))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" :store "no" } } } })
        response (index/update-mapping index "person" :mapping mapping :ignore-conflicts false)]
    (is (= (utils/ok? response)))))

(deftest create-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings {})
        response (index/update-mapping index "person" :mapping mapping)]
    (is (= true (utils/ok? response)))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest delete-index-mapping-test
  (let [index       "people"
        index-type  "person"
        _           (index/create index :mappings fixtures/people-mapping)
        response    (index/delete-mapping index, index-type)]
    (is (= true (utils/ok? response)))
    (is (nil? ((index/get-mapping index) index-type)))))

;;
;; Settings
;;

(deftest get-index-settings-test
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :settings settings :mappings fixtures/people-mapping)]
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

(deftest update-index-settings-global
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :mappings fixtures/people-mapping)
        response  (index/update-settings settings)]
    (is (= true (utils/ok? response)))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

(deftest update-index-settings-concrete
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :mappings fixtures/people-mapping)
        response  (index/update-settings index settings)]
    (is (= true (utils/ok? response)))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

;;
;; Open/close
;;

(deftest open-close-index-test
  (let [index     "people"
        _         (index/create index :mappings fixtures/people-mapping)]
    (is (= true (utils/ok? (index/open index))))
    (is (= true (utils/ok? (index/close index))))))
