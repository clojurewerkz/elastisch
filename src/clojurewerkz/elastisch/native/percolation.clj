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

(def ^:const percolator-index "_percolator")

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
