;; Copyright 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

(ns clojurewerkz.elastisch.native.percolation
  (:require [clojurewerkz.elastisch.native            :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojure.walk :as wlk])
  (:import org.elasticsearch.action.index.IndexResponse
           org.elasticsearch.action.delete.DeleteResponse
           org.elasticsearch.action.percolate.PercolateResponse
           org.elasticsearch.action.index.IndexRequestBuilder
           java.util.Map
           org.elasticsearch.client.Client
           org.elasticsearch.percolator.PercolatorService))

;;
;; API
;;

(defn register-query
  "Registers a percolator for the given index"
  ([^Client conn index query-name] (register-query conn index query-name nil))
  ([^Client conn index query-name opts]
   (let [^IndexRequestBuilder irb (doto (.prepareIndex ^Client conn
                                                       index
                                                       PercolatorService/TYPE_NAME
                                                       query-name)
                                    (.setSource ^Map (wlk/stringify-keys opts)))
         ft                       (.execute irb)
         ^IndexResponse res       (.actionGet ft)]
     (merge (cnv/index-response->map res) {:ok true}))))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [^Client conn index percolator]
  (let [ft (es/delete conn (cnv/->delete-request index
                                                 PercolatorService/TYPE_NAME
                                                 percolator))
        ^DeleteResponse res (.actionGet ft)]
    (merge (cnv/delete-response->map res) {:ok (.isFound res) :found (.isFound res)})))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
  matched against the queries you register with [[register-query]]."
  ([^Client conn index mapping-type] (percolate conn index mapping-type nil))
  ([^Client conn index mapping-type opts]
   (let [prb  (doto (.preparePercolate ^Client conn)
                (.setIndices (cnv/->string-array index))
                (.setDocumentType mapping-type)
                (.setSource ^Map (wlk/stringify-keys opts)))
         ft  (.execute prb)
         ^PercolateResponse res (.actionGet ft)]
     (cnv/percolate-response->map res))))
