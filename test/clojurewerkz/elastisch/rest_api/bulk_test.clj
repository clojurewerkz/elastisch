;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.bulk-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.bulk          :as bulk]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.rest               :as esr]
            [clojurewerkz.elastisch.query              :as q]
            [clojurewerkz.elastisch.fixtures           :as fx]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.rest.response :refer [created? acknowledged? conflict? hits-from any-hits? no-hits?]]
            [clojure.string :refer [join]]))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn are-all-successful
  [xs]
  (is (every? (fn [m] (and (:_index m)
                           (:_type m)
                           (:_id m)
                           (:status m))) xs)))

(deftest ^{:rest true :indexing true} test-bulk-insert
  (let [document          fx/person-jack
        for-index         (assoc document :_index index-name :_type index-type)
        insert-operations (bulk/bulk-index (repeat 10 for-index))
        response          (bulk/bulk insert-operations :refresh true)
        first-id          (-> response :items first :create :_id)
        get-result        (doc/get index-name index-type first-id)]
    (are-all-successful (->> response :items (map :create)))
    (is (= 10 (:count (doc/count index-name index-type))))
    (is (idx/exists? index-name))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         first-id   :_id)))

(deftest ^{:rest true :indexing true} test-bulk-with-index
  (let [document          fx/person-jack
        for-index         (assoc document :_type index-type)
        insert-operations (bulk/bulk-index (repeat 10 for-index))
        response          (bulk/bulk-with-index index-name insert-operations :refresh true)
        first-id          (-> response :items first :create :_id)
        get-result        (doc/get index-name index-type first-id)]
    (are-all-successful (->> response :items (map :create)))
    (is (= 10 (:count (doc/count index-name index-type))))
    (is (idx/exists? index-name))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         first-id   :_id)))

(deftest ^{:rest true :indexing true} test-bulk-with-index-and-type
  (let [document          fx/person-jack
        insert-operations (bulk/bulk-index (repeat 10 document))
        response          (bulk/bulk-with-index-and-type index-name index-type insert-operations :refresh true)
        first-id          (-> response :items first :create :_id)
        get-result        (doc/get index-name index-type first-id)]
    (are-all-successful (->> response :items (map :create)))
    (is (= 10 (:count (doc/count index-name index-type))))
    (is (idx/exists? index-name))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         first-id   :_id)))

(deftest ^{:rest true :indexing true} test-bulk-delete
  (let [insert-ops      (bulk/bulk-index (repeat 10 fx/person-jack))
        response        (bulk/bulk-with-index-and-type index-name index-type insert-ops :refresh true)
        docs            (->> response :items (map :create) )
        initial-count   (:count (doc/count index-name index-type))
        delete-response (bulk/bulk-with-index-and-type index-name index-type (bulk/bulk-delete docs) :refresh true)]
    (is (= 10 initial-count))
    (are-all-successful (->> response :items (map :create)))
    (is (= 0 (:count (doc/count index-name index-type))))))
