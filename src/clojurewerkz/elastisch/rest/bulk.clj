;; Copyright 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

(ns clojurewerkz.elastisch.rest.bulk
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.set :refer :all]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import clojurewerkz.elastisch.rest.Connection))

(defn ^:private bulk-with-url
  [conn url operations & args]
  (let [opts      (ar/->opts args)
        bulk-json (map json/encode operations)
        bulk-json (-> bulk-json
                      (interleave (repeat "\n"))
                      (string/join))]
    (rest/post-string conn url
                      {:body bulk-json
                       :query-params opts})))
(defn bulk
  "Performs a bulk operation"
  [^Connection conn operations & params]
  (when (not-empty operations)
    (apply bulk-with-url conn (rest/bulk-url conn) operations params)))

(defn bulk-with-index
  "Performs a bulk operation defaulting to the index specified"
  [^Connection conn index operations & params]
  (apply bulk-with-url conn (rest/bulk-url conn
                                           index) operations params))

(defn bulk-with-index-and-type
  "Performs a bulk operation defaulting to the index and type specified"
  [^Connection conn index mapping-type operations & params]
  (apply bulk-with-url conn (rest/bulk-url conn
                                           index mapping-type) operations params))

(def ^:private special-operation-keys
  [:_index :_type :_id :_retry_on_conflict :_routing :_percolate :_parent :_timestamp :_ttl])

(defn index-operation
  [doc]
  {"index" (select-keys doc special-operation-keys)})

(defn delete-operation
  [doc]
  {"delete" (select-keys doc special-operation-keys)})

(defn bulk-index
  "generates the content for a bulk insert operation"
  ([documents]
     (let [operations (map index-operation documents)
           documents  (map #(apply dissoc % special-operation-keys) documents)]
       (interleave operations documents))))

(defn bulk-delete
  "generates the content for a bulk delete operation"
  ([documents]
     (let [operations (map delete-operation documents)]
       operations)))
