(ns clojurewerkz.elastisch.rest-api.index-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response
        [clj-time.core :only [months ago]]))

(use-fixtures :each fx/reset-indexes)


(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

;;
;; put
;;

(deftest test-put-with-autocreated-index
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

(deftest test-put-with-precreated-index
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

(deftest test-put-with-missing-document-versioning-type
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1)]
    (is (conflict? response))))

(deftest test-put-with-conflicting-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1 :version_type "external")]
    (is (conflict? response))
    (is (not (ok? response)))))

(deftest test-put-with-new-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack :version 1 :version_type "external")
        _        (doc/put index-name index-type id fx/person-mary :version 2 :version_type "external")
        response (doc/put index-name index-type id fx/person-joe  :version 3 :version_type "external")]
    (is (not (conflict? response)))
    (is (ok? response))))

(deftest create-when-already-created-test
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        response (doc/put index-name index-type id fx/person-joe :op_type "create")]
    (is (conflict? response))))

(deftest test-put-with-a-timestamp
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timestamp (-> 2 months ago))]
    (is (ok? response))))

(deftest test-put-with-a-1-day-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl "1d")]
    (is (ok? response))))

(deftest test-put-with-a-10-seconds-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl 10000)]
    (is (ok? response))))

(deftest test-put-with-a-timeout
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timeout "1m")]
    (is (ok? response))))

(deftest test-put-with-refresh-set-to-true
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :refresh true)]
    (is (ok? response))))


;;
;; create
;;

(deftest put-create-autogenerate-id-test
  (let [response (doc/create index-name index-type fx/person-jack)]
    (is (ok? response))
    (is (:_id response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type  :_type
         1          :_version)))
