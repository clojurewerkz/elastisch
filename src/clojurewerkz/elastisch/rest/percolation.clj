;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest.percolation
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojurewerkz.elastisch.arguments :as ar]))

;;
;; API
;;

(defn register-query
  "Registers a percolator for the given index"
  [index percolator & args]
  (rest/put (rest/percolator-url index percolator) :body (ar/->opts args)))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [index percolator]
  (rest/delete (rest/percolator-url index percolator)))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
   matched against the queries you register with clojurewerkz.elastisch.rest.percolation/register-query."
  [index percolator & args]
  ;; rest/get won't serialize the body for us. MK.
  (rest/get (rest/index-percolation-url index percolator) :body (json/encode (ar/->opts args))))
