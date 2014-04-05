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
            [clojurewerkz.elastisch.arguments :as ar]))

(defn ^:private msearch-with-url
  [url queries & args]
  (let [opts         (ar/->opts args)
        msearch-json (map json/encode queries)
        msearch-json (-> msearch-json
                         (interleave (repeat "\n"))
                         (string/join))]
    (rest/get url
              :body msearch-json
              :query-params opts)))

(defn search
  "Performs multi search"
  [queries & params]
  (:responses (apply msearch-with-url (rest/multi-search-url) queries params)))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  [index queries & params]
  (:responses (apply msearch-with-url (rest/multi-search-url index) queries params)))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  [index mapping-type queries & params]
  (:responses (apply msearch-with-url (rest/multi-search-url index mapping-type) queries params)))
