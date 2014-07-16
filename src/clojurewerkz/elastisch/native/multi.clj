;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native.multi
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import org.elasticsearch.client.Client))

(defn search
  "Performs multi search"
  [^Client conn queries & params]
  (let [opts (ar/->opts params)
        res  (es/multi-search conn (cnv/->multi-search-request conn queries opts))]
    (cnv/multi-search-response->seq (.actionGet res))))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  [^Client conn index queries & params]
  (let [opts (ar/->opts params)
        res  (es/multi-search conn (cnv/->multi-search-request conn index queries opts))]
    (cnv/multi-search-response->seq (.actionGet res))))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  [^Client conn index mapping-type queries & params]
  (let [opts (ar/->opts params)
        res  (es/multi-search conn (cnv/->multi-search-request conn
                                                               index
                                                               mapping-type
                                                               queries opts))]
    (cnv/multi-search-response->seq (.actionGet res))))
