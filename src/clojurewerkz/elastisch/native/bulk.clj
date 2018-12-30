;; Copyright 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.native.bulk
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.native :as native]
            [clojure.string :as string]
            [clojure.set :refer :all]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.common.bulk :as common-bulk])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.action.bulk.BulkRequestBuilder
           org.elasticsearch.action.bulk.BulkResponse
           org.elasticsearch.action.index.IndexRequest
           org.elasticsearch.action.update.UpdateRequest
           org.elasticsearch.action.delete.DeleteRequest))

(defprotocol AddOperation
  (add-operation [operation bulk-builder]))

(extend-protocol AddOperation
  IndexRequest
  (add-operation [^IndexRequest operation ^BulkRequestBuilder bulk-builder]
    (.add bulk-builder operation))

  UpdateRequest
  (add-operation [^UpdateRequest operation ^BulkRequestBuilder bulk-builder]
    (.add bulk-builder operation))

  DeleteRequest
  (add-operation [^DeleteRequest operation ^BulkRequestBuilder bulk-builder]
    (.add bulk-builder operation)))

(defn add-default [doc default]
  (if-let [action (cnv/get-bulk-item-action doc)]
    (update-in doc [action] #(merge default %))
    doc))

(defn bulk
  "Performs a bulk operation"
  ([^Client conn operations] (bulk conn operations nil))
  ([^Client conn operations params]
   (let [^BulkRequestBuilder req (reduce #(add-operation %2 %1) (.prepareBulk conn)
                                         (cnv/->action-requests operations))]
     (when (:refresh params)
       (.setRefresh req true))
     (-> req
         .execute
         ^BulkResponse .actionGet
         cnv/bulk-response->map))))

(defn bulk-with-index
  "Performs a bulk operation defaulting to the index specified"
  ([^Client conn index operations] (bulk-with-index conn index operations nil))
  ([^Client conn index operations params]
   (bulk conn (map #(add-default % {:_index index}) operations) params)))

(defn bulk-with-index-and-type
  "Performs a bulk operation defaulting to the index and type specified"
  ([^Client conn index mapping-type operations] (bulk-with-index-and-type conn index mapping-type operations nil))
  ([^Client conn index mapping-type operations params]
   (bulk conn
         (map #(add-default % {:_index index :_type mapping-type}) operations)
         params)))

(def index-operation common-bulk/index-operation)

(def delete-operation common-bulk/delete-operation)

(def bulk-index common-bulk/bulk-index)

(def bulk-update common-bulk/bulk-update)

(def bulk-delete common-bulk/bulk-delete)

(def bulk-create common-bulk/bulk-create)
