(ns elastisch.test.core
  (:require [clj-http.client         :as http]
            [elastisch.rest-client   :as rest]
            [elastisch.index         :as index]
            [elastisch.urls          :as urls]
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
    (is (utils/ok? response))
    (is (index/exists? index))))

(deftest delete-index-test
  (let [index    "people"
        _        (index/create index :mappings fixtures/people-mapping)
        response (index/delete index)]
    (is (utils/ok? response))
    (not (index/exists? index))))

;;
;; Mappings
;;

(deftest get-index-mapping-test
  (let [index    "people"
        mappings fixtures/people-mapping
        response (index/create index :mappings mappings)]
    (is (utils/ok? response))
    (is (= mappings (:people (index/get-mapping index))))
    (is (= mappings (:people (index/get-mapping [index, "shmeople"]))))
    (is (= mappings (index/get-mapping index "person")))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" } } } })
        response (index/update-mapping index "person" :mapping mapping)]
    (is (utils/ok? response))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings { :person { :properties { :first-name { :type "string" :store "no" } } } })
        response (index/update-mapping index "person" :mapping mapping :ignore-conflicts false)]
    (not (utils/ok? response))))

(deftest create-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings {})
        response (index/update-mapping index "person" :mapping mapping)]
    (is (utils/ok? response))
    (is (= mapping (:people (index/get-mapping index))))))

(deftest delete-index-mapping-test
  (let [index       "people"
        index-type  "person"
        _           (index/create index :mappings fixtures/people-mapping)
        response    (index/delete-mapping index, index-type)]
    (is (utils/ok? response))
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
    (is (utils/ok? response))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

(deftest update-index-settings-concrete
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (index/create index :mappings fixtures/people-mapping)
        response  (index/update-settings index settings)]
    (is (utils/ok? response))
    (is (= "1s" (:index.refresh_interval (:settings (:people (index/get-settings "people"))))))))

;;
;; Open/close
;;

(deftest open-close-index-test
  (let [index     "people"
        _         (index/create index :mappings fixtures/people-mapping)]
    (is (utils/ok? (index/open index)))
    (is (utils/ok? (index/close index)))))

;;
;; Utils
;;
(deftest join-names-test
  (is (= "name" (utils/join-names "name")))
  (is (= "name1,name2" (utils/join-names ["name1", "name2"]))))

(deftest join-hash-test
  (is (= "a=1&b=2&c=3&d=5&e=4" (utils/join-hash (sorted-map :a 1 :b 2 :c 3 :e 4 :d 5 )))))


