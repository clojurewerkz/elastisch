(ns clojurewerkz.elastisch.rest.percolation
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]))

;;
;; API
;;

(defn register-query
  "Registers a percolator for the given index"
  [index percolator & {:as options}]
  (rest/put (rest/percolator-url index percolator) :body options))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [index percolator]
  (rest/delete (rest/percolator-url index percolator)))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
   matched against the queries you register with clojurewerkz.elastisch.rest.percolation/register-query."
  [index percolator & {:as options}]
  ;; rest/get won't serialize the body for us. MK.
  (rest/get (rest/index-percolation-url index percolator) :body (json/encode options)))
