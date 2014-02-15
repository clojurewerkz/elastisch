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
            [clojure.walk :as wlk])
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
  [index query-name & {:as source}]
  (let [^IndexRequestBuilder irb (doto (.prepareIndex ^Client es/*client*
                                                      percolator-index
                                                      index
                                                      query-name)
                                   (.setSource ^Map (wlk/stringify-keys source)))
        ft                       (.execute irb)
        ^IndexResponse res       (.actionGet ft)]
    (merge (cnv/index-response->map res) {:ok true})))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [index percolator]
  (let [ft (es/delete (cnv/->delete-request percolator-index
                                            index
                                            percolator))
        ^DeleteResponse res (.actionGet ft)]
    (merge (cnv/delete-response->map res) {:ok true})))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
   matched against the queries you register with clojurewerkz.elastisch.rest.percolation/register-query."
  [index mapping-type & {:as options}]
  (let [ft (es/percolate (cnv/->percolate-request index
                                                  mapping-type
                                                  options))
        ^PercolateResponse res (.actionGet ft)]
    (cnv/percolate-response->map res)))
