(ns elastisch.test.core
  (:require [clj-http.client         :as http]
            [elastisch.rest-client   :as rest]
            [elastisch.index         :as index]
            [elastisch.urls          :as urls]
            [elastisch.test.fixtures :as fixtures]
            )
  (:use [elastisch.core]
        [clojure.test]
))

(use-fixtures :each fixtures/delete-people-index)

(defn ok?
  [response]
  (:ok response))

(deftest create-index-test
  (let [index    "people"
        mappings fixtures/people-mapping
        response (index/create index :mappings mappings)]
    (is (= true (ok? response)))
    (is (= true (index/exists? index)))))

(deftest get-index-mapping-test
  (let [index    "people"
        mappings fixtures/people-mapping
        _        (index/create index :mappings mappings)]
    (is (= mappings (:people (index/get-mapping index))))
    (is (= mappings (:people (index/get-mapping [index, "shmeople"]))))
    (is (= mappings (index/get-mapping index "person")))))

(deftest update-index-mapping-test
  (let [index    "people"
        mapping  fixtures/people-mapping
        _        (index/create index :mappings {})
        __       (index/update-mapping index "person" :mapping mapping)]
    (is (= mapping (:people (index/get-mapping index))))))

(deftest get-index-settings-test
  (let [index     "people"
        settings  { :index { :refresh-interval "1s" } }
        _         (index/create index :settings settings :mappings {})]
    (is (= "1s" (:index.refresh-interval (:settings (:people (index/get-settings "people"))))))))

(deftest open-close-index
  (let [index     "people"
        _         (index/create index :mappings fixtures/people-mapping)]
    (is (ok? (index/open index))
    (is (ok? (index/close index))))))


;; def delete-index-mapping-test