;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest.multi
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import clojurewerkz.elastisch.rest.Connection))

(defn ^:private msearch-with-url
  [conn url queries & args]
  (let [opts         (ar/->opts args)
        msearch-json (map json/encode queries)
        msearch-json (->> msearch-json
                          (string/join "\n"))]
    (rest/get conn url
              {:body msearch-json
               :query-params opts})))

(defn search
  "Performs multi search"
  [conn queries & params]
  (:responses (apply msearch-with-url conn (rest/multi-search-url conn) queries params)))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  [^Connection conn index queries & params]
  (:responses (apply msearch-with-url conn (rest/multi-search-url conn
                                                                  index) queries params)))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  [^Connection conn index mapping-type queries & params]
  (:responses (apply msearch-with-url conn (rest/multi-search-url conn
                                                                  index mapping-type)
                     queries params)))
