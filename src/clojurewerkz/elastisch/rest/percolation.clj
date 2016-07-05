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

(ns clojurewerkz.elastisch.rest.percolation
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json])
  (:import clojurewerkz.elastisch.rest.Connection))

;;
;; API
;;

(defn register-query
  "Registers a percolator for the given index"
  ([^Connection conn index percolator] (register-query conn index percolator nil))
  ([^Connection conn index percolator opts]
   (rest/put conn (rest/percolator-url conn
                                       index percolator)
             {:body opts})))

(defn unregister-query
  "Unregisters a percolator query for the given index"
  [^Connection conn index percolator]
  (rest/delete conn (rest/percolator-url conn
                                         index percolator)))

(defn percolate
  "Percolates a document and see which queries match on it. The document is not indexed, just
  matched against the queries you register with [[register-query]]."
  ([^Connection conn index percolator] (percolate conn index percolator nil))
  ([^Connection conn index percolator opts]
   ;; rest/get won't serialize the body for us. MK.
   (rest/get conn (rest/index-percolation-url conn
                                              index percolator)
             {:body (json/encode opts)})))

(defn percolate-existing
  "Percolates an existing document and sees which queries match on it."
  [^Connection conn index percolator id]
  (rest/get conn (rest/existing-doc-index-percolation-url conn
                                                          index percolator id)))
