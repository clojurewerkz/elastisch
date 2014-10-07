;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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
