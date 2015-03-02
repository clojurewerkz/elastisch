;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.bulk-test
  (:require [clojurewerkz.elastisch.native.bulk     :refer :all]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer [created?]]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(defn are-all-successful
  [xs]
  (is (every? (fn [m] (and (:_index m)
                           (:_type m)
                           (:_id m)
                           (:status m))) xs))
  (is (every? created? xs)))

(let [conn (th/connect-native-client)]
  (deftest ^{:native true :indexing true} test-bulk-indexing
    (let [person fx/person-jack
          for-index (assoc person :_type index-type :_index index-name)
          bulk-operations (bulk-index (repeat 2 for-index))]
      (is (= 2 (count (:items (bulk conn bulk-operations {:refresh true})))))
      (is (= 2 (:count (doc/count conn index-name index-type))))))

  (deftest ^{:native true :indexing true} test-bulk-with-index
    (let [document fx/person-jack
          for-index (assoc document :_type index-type)
          insert-operations (bulk-index (repeat 10 for-index))
          response (bulk-with-index conn index-name insert-operations {:refresh true})
          first-id (-> response :items first :create :_id)]
      (is (= 10 (:count (doc/count conn index-name index-type))))
      (is (= false (:has-failures? response)))
      (is (= 10 (count (filter #(= "create" (:op-type %)) (:items response)))))
      (is (idx/exists? conn index-name))))

  (deftest ^{:native true :indexing true} test-bulk-with-index-and-type
    (let [document fx/person-jack
          insert-operations (bulk-index (repeat 10 document))
          response (bulk-with-index-and-type conn index-name index-type insert-operations {:refresh true})]
      (is (= 10 (:count (doc/count conn index-name index-type))))
      (is (= false (:has-failures? response)))
      (is (= 10 (count (filter #(= "create" (:op-type %)) (:items response)))))
      (is (idx/exists? conn index-name))))

  (deftest ^{:native true :indexing true} test-bulk-delete
    (let [insert-ops (bulk-index (repeat 10 fx/person-jack))
          response (bulk-with-index-and-type conn index-name index-type insert-ops {:refresh true})
          docs (->> response :items)
          initial-count (:count (doc/count conn index-name index-type))
          delete-response (bulk-with-index-and-type conn index-name index-type (bulk-delete docs) {:refresh true})]
      (is (= 10 initial-count))
      (is (= false (:has-failures? response)))
      (is (= 0 (:count (doc/count conn index-name index-type)))))))
