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

(ns clojurewerkz.elastisch.rest.multi
  (:require [clojurewerkz.elastisch.rest :as rest]
            [cheshire.core :as json]
            [clojure.string :as string])
  (:import clojurewerkz.elastisch.rest.Connection))

(defn ^:private msearch-with-url
  [conn url queries opts]
  (let [body (string/join "\n" (doall (map json/encode queries)))]
    (rest/get conn url
              ;; multi-search is sensitive to trailing new line. MK.
              {:body (str body "\n")
               :query-params opts})))

(defn search
  "Performs multi search"
  ([conn queries] (search conn queries nil))
  ([conn queries params]
   (:responses (msearch-with-url conn (rest/multi-search-url conn) queries params))))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  ([^Connection conn index queries] (search-with-index conn index queries nil))
  ([^Connection conn index queries params]
   (:responses (msearch-with-url conn (rest/multi-search-url conn
                                                             index) queries params))))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  ([^Connection conn index mapping-type queries] (search-with-index-and-type conn index mapping-type queries nil))
  ([^Connection conn index mapping-type queries params]
   (:responses (msearch-with-url conn (rest/multi-search-url conn
                                                             index mapping-type)
                      queries params))))
