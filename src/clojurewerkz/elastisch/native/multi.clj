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

(ns clojurewerkz.elastisch.native.multi
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client))

(defn search
  "Performs multi search"
  ([^Client conn queries] (search conn queries nil))
  ([^Client conn queries opts]
   (let [res  (es/multi-search conn (cnv/->multi-search-request conn queries opts))]
     (cnv/multi-search-response->seq (.actionGet res)))))

(defn search-with-index
  "Performs multi search defaulting to the index specified"
  ([^Client conn index queries] (search-with-index conn index queries nil))
  ([^Client conn index queries opts]
   (let [res (es/multi-search conn (cnv/->multi-search-request conn index queries opts))]
     (cnv/multi-search-response->seq (.actionGet res)))))

(defn search-with-index-and-type
  "Performs multi search defaulting to the index and type specified"
  ([^Client conn index mapping-type queries] (search-with-index-and-type conn index mapping-type queries nil))
  ([^Client conn index mapping-type queries opts]
   (let [res  (es/multi-search conn (cnv/->multi-search-request conn
                                                                index
                                                                mapping-type
                                                                queries opts))]
     (cnv/multi-search-response->seq (.actionGet res)))))
