(ns clojurewerkz.elastisch.test.document
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.test.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response))

(use-fixtures :each fx/delete-people-index)
(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

;;
;; put
;;

(deftest put-autocreate-index-test
  (let [id         "1"
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (is (idx/exists? index-name))

    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest put-precreated-index-test
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest put-versioned-test
  (let [id       "1"
        document fx/person-joe
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1)]
    (is (conflict? response))))

(deftest create-when-already-created-test
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        response (doc/put index-name index-type id fx/person-joe :op_type "create")]
    (is (conflict? response))))

;;
;; create
;;

(deftest put-create-autogenerate-id-test
  (let [response (doc/create index-name index-type fx/person-jack)]
    (is (ok? response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type  :_type
         1          :_version)))

;;
;; get, present?
;;

(deftest get-non-existing-test
  (is (nil? (doc/get index-name index-type "1"))))

(deftest present-on-non-existing-test
  (is (not (doc/present? index-name index-type "1"))))

(deftest present-on-existing-test
  (doc/put index-name index-type "1" fx/person-jack)
  (is (doc/present? index-name index-type "1")))

;;
;; delete
;;

(deftest delete-test
  (let [id "1"]
    (doc/put index-name index-type id fx/person-jack)
    (is (doc/present? index-name index-type id))
    (is (ok? (doc/delete index-name index-type id)))
    (is (not (doc/present? index-name index-type id)))))

;;
;; mget
;;

(deftest multi-get-test
  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)
  (let [mget-result (doc/multi-get
                     [ { :_index index-name :_type index-type :_id "1"  }
                       { :_index index-name :_type index-type :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name
                                   [ { :_type index-type :_id "1"  }
                                     { :_type index-type :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name index-type
                                   [ { :_id "1"  } { :_id "2" } ])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result))))))

;;
;; term query
;;

(deftest basic-term-query-test
  (idx/create index-name :mappings fx/people-mapping)

  (doc/put index-name index-type "1" fx/person-jack)
  (doc/put index-name index-type "2" fx/person-mary)
  (doc/put index-name index-type "3" fx/person-joe)

  (idx/refresh index-name)

  (let [result (doc/search index-name index-type :query (q/term :biography "avoid"))]
    (is (any-hits? result))
    (is (= fx/person-jack (:_source (first (hits-from result)))))))

;;
;; replace
;;

(deftest ^{:focus true} test-replacing-documents
  (let [id      "1"
        new-bio "Such a brilliant person"]
    (idx/create index-name :mappings fx/people-mapping)

    (doc/put index-name index-type "1" fx/person-jack)
    (doc/put index-name index-type "2" fx/person-mary)
    (doc/put index-name index-type "3" fx/person-joe)

    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))
    (is (no-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    (doc/replace index-name index-type id (assoc fx/person-joe :biography new-bio))
    (idx/refresh index-name)
    (is (any-hits? (doc/search index-name index-type :query (q/term :biography "brilliant"))))
    ;; TODO: investigate this. MK.
    #_ (is (no-hits? (doc/search index-name index-type :query (q/term :biography "nice"))))))


;; deftest optional-type
;; deftest fields
;; deftest routing
;; deftest preference
;; deftest refresh
