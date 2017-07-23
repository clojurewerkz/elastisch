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

(ns clojurewerkz.elastisch.rest.response
  (:require [clojurewerkz.support.http.statuses :as statuses]))

;;
;; API
;;

(defn created?
  [m]
  (true? (or (get m :created)
             (= 201 (get m :status)))))

(defn acknowledged?
  [m]
  (:acknowledged m))

(defn created-or-acknowledged?
  [m]
  (or (created? m)
      (acknowledged? m)))


(defn all-shards-report-success?
  [m]
  (= (get-in m [:_shards :failed]) 0))

(defn ok?
  [m]
  (or (created-or-acknowledged? m)
      (and (all-shards-report-success? m)
           (nil? (:error m)))))

(defn conflict?
  [m]
  (let [s (:status m)]
    (and s (statuses/conflict? s))))

(defn found?
  [m]
  (true? (get m :found)))

(defn not-found?
  [m]
  (let [s (:status m)]
    (or (false? (:found m))
        (and s (statuses/missing? s)))))

(defn accepted?
  [m]
  (:accepted m))

(defn valid?
  "Returns `true` if a validation query response indicates valid query, `false` otherwise"
  [m]
  (:valid m))

(defn timed-out?
  [m]
  (:timed_out m))


(defn total-hits
  "Returns number of search hits from a response"
  [m]
  (get-in m [:hits :total]))

(defn count-from
  "Returns total number of search hits from a response"
  [m]
  (get m :count))

(defn any-hits?
  "Returns `true` if a response has any search hits, `false` otherwise"
  [m]
  (> (total-hits m) 0))

(def no-hits? (complement any-hits?))

(defn hits-from
  "Returns search hits from a response as a collection. To retrieve hits overview, get the `:hits`
  key from the response"
  [m]
  (get-in m [:hits :hits]))

(defn ids-from
  "Returns search hit ids from a response"
  [m]
  (if (any-hits? m)
    (set (map :_id (hits-from m)))
    #{}))

(defn matches-from
  "Returns matches from a percolation response as a collection."
  [m]
  (get m :matches []))

(defn aggregations-from
  "Returns aggregations from a search response"
  [m]
  (get m :aggregations []))

(defn aggregation-from
  "Return a single aggregation from a search response"
  [m name]
  (get-in m [:aggregations name] []))

(defn source-from
  "Returns document source from a get response"
  [m]
  (get m :_source))

(defn sources-from
  "Returns search hit sources from a response as a collection"
  [m]
  (map source-from (hits-from m)))
