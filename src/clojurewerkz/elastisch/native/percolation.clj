;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native.percolation
  (:require [clojurewerkz.elastisch.native            :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojure.walk :as wlk]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import org.elasticsearch.action.index.IndexResponse
           org.elasticsearch.action.delete.DeleteResponse
           org.elasticsearch.action.percolate.PercolateResponse
           org.elasticsearch.action.index.IndexRequestBuilder
           java.util.Map
           org.elasticsearch.client.Client))

(def ^:const percolator-index ".percolator")

;;
;; API
;;

(defn register-query
  "Registers a percolator for the given index"
  [index query-name & args]
  (let [opts                     (ar/->opts args)
        ^IndexRequestBuilder irb (doto (.prepareIndex ^Client es/*client*
                                                      index
                                                      percolator-index
                                                      query-name)
                                   (.setSource ^Map (wlk/stringify-keys opts)))
        ft                       (.execute irb)
        ^IndexResponse res       (.actionGet ft)]
    (merge (cnv/index-response->map res) {:ok true})))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [index percolator]
  (let [ft (es/delete es/*client* (cnv/->delete-request percolator-index
                                            index
                                            percolator))
        ^DeleteResponse res (.actionGet ft)]
    (merge (cnv/delete-response->map res) {:ok (.isFound res) :found (.isFound res)})))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
   matched against the queries you register with clojurewerkz.elastisch.rest.percolation/register-query."
  [index mapping-type & args]
  (let [opts (ar/->opts args)
        prb  (doto (.preparePercolate ^Client es/*client*)
               (.setIndices (cnv/->string-array index))
               (.setDocumentType mapping-type)
               (.setSource ^Map (wlk/stringify-keys opts)))
        ft  (.execute prb)
        ^PercolateResponse res (.actionGet ft)]
    (cnv/percolate-response->map res)))
