\(ns clojurewerkz.elastisch.test.document
  (:require [clojurewerkz.elastisch.document      :as document]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as index]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.query         :as query]
            [clojurewerkz.elastisch.test.fixtures :as fixtures])
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
        response   (document/put index-name index-type id document)
        get-result (document/get index-name index-type id)]
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
        response   (document/put index-name index-type id document)
        get-result (document/get index-name index-type id)]
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
        _        (document/put index-name index-type id fixtures/person-jack)
        _        (document/put index-name index-type id fixtures/person-mary)
        response (document/put index-name index-type id fixtures/person-joe :version 1)]
    (is (utils/conflict? response))))

(deftest create-when-already-created-test
  (let [id       "1"
        _        (document/put index-name index-type id fixtures/person-jack)
        response (document/put index-name index-type id fixtures/person-joe :op_type "create")]
    (is (utils/conflict? response))))

;;
;; create
;;

(deftest put-create-autogenerate-id-test
  (let [response (document/create index-name index-type fixtures/person-jack)]
    (is (utils/ok? response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type  :_type
         1          :_version)))

;;
;; get, present?
;;

(deftest get-non-existing-test
  (is (nil? (document/get index-name index-type "1"))))

(deftest present-on-non-existing-test
  (is (not (document/present? index-name index-type "1"))))

(deftest present-on-existing-test
  (document/put index-name index-type "1" fixtures/person-jack)
  (is (document/present? index-name index-type "1")))

;;
;; delete
;;

(deftest delete-test
  (let [id "1"]
    (document/put index-name index-type id fixtures/person-jack)
    (is (document/present? index-name index-type id))
    (is (utils/ok? (document/delete index-name index-type id)))
    (is (not (document/present? index-name index-type id)))))

;;
;; mget
;;

(deftest multi-get-test
  (document/put index-name index-type "1" fixtures/person-jack)
  (document/put index-name index-type "2" fixtures/person-mary)
  (document/put index-name index-type "3" fixtures/person-joe)
  (let [mget-result (document/multi-get
                     [ { :_index index-name :_type index-type :_id "1"  }
                       { :_index index-name :_type index-type :_id "2" } ])]
    (is (= fixtures/person-jack (:_source (first mget-result))))
    (is (= fixtures/person-mary (:_source (second mget-result)))))
  (let [mget-result (document/multi-get index-name
                     [ { :_type index-type :_id "1"  }
                       { :_type index-type :_id "2" } ])]
    (is (= fixtures/person-jack (:_source (first mget-result))))
    (is (= fixtures/person-mary (:_source (second mget-result)))))
  (let [mget-result (document/multi-get index-name index-type
                     [ { :_id "1"  } { :_id "2" } ])]
    (is (= fixtures/person-jack (:_source (first mget-result))))
    (is (= fixtures/person-mary (:_source (second mget-result))))))

;;
;; query
;;

(deftest search-test
  (index/create index-name :mappings fixtures/people-mapping)

  (document/put index-name index-type "1" fixtures/person-jack)
  (document/put index-name index-type "2" fixtures/person-mary)
  (document/put index-name index-type "3" fixtures/person-joe)

  (index/refresh index-name)

  (let [result (document/search index-name index-type :query (query/term :biography "avoid"))]
    (is (= fixtures/person-jack (:_source (first (:hits (:hits result))))))))

;; deftest optional-type
;; deftest fields
;; deftest routing
;; deftest preference
;; deftest refresh
