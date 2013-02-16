(ns clojurewerkz.elastisch.rest.bulk
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string])
  (:use     clojure.set
            [clojurewerkz.elastisch.rest.utils :only [join-names]]
            [clojurewerkz.elastisch.rest.response :only [not-found?]]))

(defn ^:private bulk-with-url
  [url operations & {:as params}]
  (let [bulk-json (map json/encode operations)
        bulk-json (-> bulk-json
                      (interleave (repeat "\n"))
                      (string/join))]
    (rest/post-string url
                      :body bulk-json
                      :query-params params)))
(defn bulk
  "Performs a bulk operation"
  [operations & params]
  (apply bulk-with-url (rest/bulk-url) operations params))

(defn bulk-with-index
  "Performs a bulk operation defaulting to the index specified"
  [index operations & params]
  (apply bulk-with-url (rest/bulk-url index) operations params))

(defn bulk-with-index-and-type
  "Performs a bulk operation defaulting to the index and type specified"
  [index mapping-type operations & params]
  (apply bulk-with-url (rest/bulk-url index mapping-type) operations params))
