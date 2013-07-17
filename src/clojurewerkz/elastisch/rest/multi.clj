(ns clojurewerkz.elastisch.rest.multi
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string]))

(defn ^:private msearch-with-url
  [url queries & {:as params}]
  (let [msearch-json (map json/encode queries)
        msearch-json (-> msearch-json
                         (interleave (repeat "\n"))
                         (string/join))]
    (rest/get url
              :body msearch-json
              :query-params params)))

(defn search
  "Performs multi search"
  [queries & params]
  (apply msearch-with-url (rest/multi-search-url) queries params))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  [index queries & params]
  (apply msearch-with-url (rest/multi-search-url index) queries params))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  [index mapping-type queries & params]
  (apply msearch-with-url (rest/multi-search-url index mapping-type) queries params))