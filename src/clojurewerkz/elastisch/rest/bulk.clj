;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

(ns clojurewerkz.elastisch.rest.bulk
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string]
            [clojure.set :refer :all]
            [clojurewerkz.elastisch.common.bulk :as common-bulk])
  (:import clojurewerkz.elastisch.rest.Connection))

(defn ^:private bulk-with-url
  ([conn url operations] (bulk-with-url conn url operations nil))
  ([conn url operations opts]
   (let [bulk-json (map json/encode operations)
         bulk-json (-> bulk-json
                       (interleave (repeat "\n"))
                       (string/join))]
     (rest/post-string conn url
                       {:body bulk-json
                        :query-params opts}))))
(defn bulk
  "Performs a bulk operation"
  ([^Connection conn operations] (bulk conn operations nil))
  ([^Connection conn operations params]
   (when (not-empty operations)
     (bulk-with-url conn (rest/bulk-url conn) operations params))))

(defn bulk-with-index
  "Performs a bulk operation defaulting to the index specified"
  ([^Connection conn index operations] (bulk-with-index conn index operations nil))
  ([^Connection conn index operations params]
   (bulk-with-url conn (rest/bulk-url conn
                                      index) operations params)))

(defn bulk-with-index-and-type
  "Performs a bulk operation defaulting to the index and type specified"
  ([^Connection conn index mapping-type operations] (bulk-with-index-and-type conn index mapping-type operations nil))
  ([^Connection conn index mapping-type operations params]
   (bulk-with-url conn (rest/bulk-url conn
                                      index mapping-type) operations params)))

(def index-operation common-bulk/index-operation)

(def delete-operation common-bulk/delete-operation)

(def bulk-index common-bulk/bulk-index)

(def bulk-update common-bulk/bulk-update)

(def bulk-delete common-bulk/bulk-delete)

(def bulk-create common-bulk/bulk-create)
