(ns elastisch.test.core
  (:require [elastisch.core          :as core]
            [elastisch.rest-client   :as rest]
            [elastisch.index         :as index]
            [elastisch.urls          :as urls]
            [elastisch.utils         :as utils]
            [elastisch.test.fixtures :as fixtures])
  (:use [clojure.test]))

(use-fixtures :each fixtures/delete-people-index)
(def index-name "people")
(def index-type  "person")

;;
;; put
;;

(deftest put-autocreate-index-test
  (let [id         "1"
        document   fixtures/person-jack
        response   (core/put index-name index-type id document)
        get-result (core/get index-name index-type id)]
    (is (utils/ok? response))
    (is (index/exists? index-name))

    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest put-precreated-index-test
  (let [id       "1"
        _        (index/create index-name :mappings fixtures/people-mapping)
        document   fixtures/person-jack
        response   (core/put index-name index-type id document)
        get-result (core/get index-name index-type id)]
    (is (utils/ok? response))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest put-versioned-test
  (let [id       "1"
        document fixtures/person-joe
        _        (core/put index-name index-type id fixtures/person-jack)
        _        (core/put index-name index-type id fixtures/person-mary)
        response (core/put index-name index-type id fixtures/person-joe :version 1)]
    (is (utils/conflict? response))))

(deftest create-when-already-created-test
  (let [id       "1"
        _        (core/put index-name index-type id fixtures/person-jack)
        response (core/put index-name index-type id fixtures/person-joe :op_type "create")]
    (is (utils/conflict? response))))

;;
;; create
;;

(deftest put-create-autogenerate-id-test
  (let [response (core/create index-name index-type fixtures/person-jack)]
    (is (utils/ok? response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type  :_type
         1          :_version)))

;;
;; get, present?
;;

(deftest get-non-existing-test
  (is (nil? (core/get index-name index-type "1"))))

(deftest present-on-non-existing-test
  (is (not (core/present? index-name index-type "1"))))

(deftest present-on-existing-test
  (core/put index-name index-type "1" fixtures/person-jack)
  (is (core/present? index-name index-type "1")))

;;
;; delete
;;

(deftest delete-test
  (let [id "1"]
    (core/put index-name index-type id fixtures/person-jack)
    (is (core/present? index-name index-type id))
    (is (utils/ok? (core/delete index-name index-type id)))
    (is (not (core/present? index-name index-type id)))))

;; deftest optional-type
;; deftest fields
;; deftest routing
;; deftest preference
;; deftest refresh


