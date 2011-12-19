(ns elastisch.test.core
  (:require [elastisch.rest-client   :as rest]
            [elastisch.index         :as index]
            [elastisch.urls          :as urls]
            [elastisch.utils         :as utils]
            [elastisch.test.fixtures :as fixtures])
  (:use [elastisch.core]
        [clojure.test]))

(use-fixtures :each fixtures/delete-people-index)

(deftest put-record-autocreate-index-test
  (let [index      "people"
        type       "person"
        id         "1"
        document   fixtures/person-jack
        response   (put-record index type id document)
        get-result (get-record index type id)]
    (is (= true (utils/ok? response)))
    (is (= true (index/exists? index)))

    (are [expected actual] (= expected (actual get-result))
         document :_source
         index    :_index
         type     :_type
         id       :_id
         true     :exists)))

(deftest put-record-precreated-index-test
  (let [index    "people"
        type     "person"
        id       "1"        
        _        (index/create index :mappings fixtures/people-mapping)
        document   fixtures/person-jack
        response   (put-record index type id document)
        get-result (get-record index type id)]
    (is (= true (utils/ok? response)))
    (are [expected actual] (= expected (actual get-result))
         document :_source
         index    :_index
         type     :_type
         id       :_id
         true     :exists)))

(deftest put-record-versioned-test
  (let [index    "people"
        type     "person"
        id       "1"
        document fixtures/person-joe
        response_v1     (put-record index type id fixtures/person-jack)
        response_middle (put-record index type id fixtures/person-mary)
        response        (put-record index type id fixtures/person-joe :version 1)]
    (is (= false (utils/ok? response)))))

;; deftest optional-type
;; deftest fields
;; deftest routing
;; deftest preference
;; deftest refresh


