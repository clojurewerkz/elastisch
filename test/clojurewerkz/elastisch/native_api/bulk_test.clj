;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
            [clojurewerkz.elastisch.native.response :refer [created? source-from]]
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
      (is (= 0 (:count (doc/count conn index-name index-type))))))

  (deftest ^{:native true :indexing true} test-bulk-updating
    (let [id "1"
          for-insert (assoc fx/person-jack :_id id :biography "original")
          insert-ops (bulk-index [for-insert])
          response (bulk-with-index-and-type conn index-name index-type insert-ops {:refresh true})
          for-update (assoc for-insert :biography "updated")
          update-ops (bulk-update [for-update])
          update-response (bulk-with-index-and-type conn index-name index-type update-ops {:refresh true})]
      (is (= "updated" (-> (doc/get conn index-name index-type id) source-from :biography)))))

  (deftest ^{:native true :indexing true} test-bulk-updating-with-doc-as-upsert
    (let [id "1"
          for-update (assoc fx/person-jack :_id id :biography "original" :_doc_as_upsert true)
          update-ops (bulk-update [for-update])
          update-response (bulk-with-index-and-type conn index-name index-type update-ops {:refresh true})]
      (is (= "original" (-> (doc/get conn index-name index-type id) source-from :biography)))))

  (deftest ^{:native true :indexing true :scripting true} test-bulk-update-with-scripting
    (let [id "1"
          for-insert (assoc fx/person-jack :_id id)
          insert-ops (bulk-index [for-insert])
          response (bulk-with-index-and-type conn index-name index-type insert-ops {:refresh true})
          for-update (assoc for-insert
                            :_script "ctx._source[\"ran_script\"] = true")
          update-ops (bulk-update [for-update])
          update-response (bulk-with-index-and-type conn index-name index-type update-ops {:refresh true})]
      (is (= false (:has-failures? update-response))) ;;scripting must be switched on
      (is (-> (doc/get conn index-name index-type id) source-from :ran_script))))

  (deftest ^{:native true :indexing true :scripting true} test-bulk-update-with-scripted-upsert
    (let [id "2"
          script "ctx._source[\"ran_script\"] = true"
          for-update (assoc fx/person-jack :_id id :_scripted_upsert true :_script script)
          update-ops (bulk-update [for-update])
          update-response (bulk-with-index-and-type conn index-name index-type update-ops {:refresh true})]
      (is (-> (doc/get conn index-name index-type id) source-from :ran_script))))

  (deftest ^{:native true :indexing true :scripting true} test-bulk-update-with-script-params
    (let [id "3"
          for-insert (assoc fx/person-jack :_id id)
          insert-ops (bulk-index [for-insert])
          response (bulk-with-index-and-type conn index-name index-type insert-ops {:refresh true})
          script "ctx._source[\"ran_script\"] = val"
          for-update (assoc fx/person-jack :_id id :_script script :_script_params {:val "param1"})
          update-ops (bulk-update [for-update])
          update-response (bulk-with-index-and-type conn index-name index-type update-ops {:refresh true})]
      (is (= "param1" (-> (doc/get conn index-name index-type id) source-from :ran_script))))))
