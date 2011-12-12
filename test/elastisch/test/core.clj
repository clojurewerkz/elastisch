(ns elastisch.test.core
  (:require [clj-http.client        :as http]
            [elastisch.rest-client  :as rest]
            [elastisch.index        :as index]
            [elastisch.urls         :as urls])
  (:use [elastisch.core]
        [clojure.test]
        [elastisch.test.fixtures]))

(use-fixtures :each delete-people-index)

(defn ok?
  [response]
  (:ok response))

(deftest create-index-test
  (let [index    "people"
        mappings people-mapping
        response (index/create index :mappings mappings)]
    (is (= true (ok? response)))
    (is (= true (index/exists? index)))))

(deftest get-index-mapping-test
  (let [index    "people"
        mappings people-mapping
        _        (index/create index :mappings mappings)]
    (is (= mappings (:people (index/mapping index))))
    (is (= mappings (:people (index/mapping [index, "shmeople"]))))
    (is (= mappings (index/mapping index "person")))))

(deftest get-index-settings-test)
