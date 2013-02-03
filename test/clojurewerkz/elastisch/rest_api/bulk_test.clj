(ns clojurewerkz.elastisch.rest-api.bulk-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.rest               :as esr]
            [clojurewerkz.elastisch.query         :as q]

            [clojurewerkz.elastisch.fixtures :as fx]
            [cheshire.core :as json]
            [clj-http.client :as http])
  (:use clojure.test
        [clojurewerkz.elastisch.rest.response :only [ok? acknowledged? conflict? hits-from any-hits? no-hits?]]
        [clojure.string :only [join]]
        ))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn index-operation
  [index mapping-type {id :_id} ]
  {"index"  {"_index" index
                      "_type"  mapping-type
                      "_id"    id}})

(defn delete-operation
  [index mapping-type id ]
  {"delete"  {"_index" index
                      "_type"  mapping-type
                      "_id"    id}})

(defn bulk-index
  "generates the content for a bulk operation
   currently :index and :delete are supported"
  ([index mapping-type documents]
     (let [operations (map (partial index-operation index mapping-type)
                           documents)]
       (interleave operations documents))))

(defn bulk-delete
  "generates the content for a bulk operation
   currently :index and :delete are supported"
  ([index mapping-type documents]
     (let [operations (map (partial delete-operation index mapping-type) documents)]
       operations)))


(deftest ^{:indexing true} test-bulk-insert
  (let [document          fx/person-jack
        insert-operations (bulk-index index-name index-type (repeat 10 document))
        response          (doc/bulk insert-operations :refresh true)
        first-id          (-> response :items first :create :_id)
        get-result        (doc/get index-name index-type first-id)]
    (is (every? ok? (->> response :items (map :create))))

    (is (= 10 (:count (doc/count index-name index-type))))
    (is (idx/exists? index-name))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         first-id   :_id
         true       :exists)))

(deftest ^{:indexing true} test-bulk-delete
  (let [document        fx/person-jack
        response        (doc/bulk (bulk-index index-name index-type (repeat 10 document)) :refresh true)
        docs            (->> response :items (map :create) )
        initial-count   (:count (doc/count index-name index-type))
        delete-response (doc/bulk (bulk-delete index-name index-type  (map :_id docs)) :refresh true)]
    (is (every? ok? (->> response :items (map :create))))
    (is (= 10 initial-count))

    (is (every? ok? (->> response :items (map :create))))
    (is (= 0 (:count (doc/count index-name index-type))))
    ))
