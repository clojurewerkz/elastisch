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

(ns clojurewerkz.elastisch.rest.admin
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]])
  (:import clojurewerkz.elastisch.rest.Connection))

;;
;; API
;;

(defn cluster-health
  "see <http://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-health.html>

  Examples:

  ```clojure
  (require '[clojurewerkz.elastisch.rest.admin :as admin])

  (admin/cluster-health conn)
  (admin/cluster-health conn {:index \"index1\"})
  (admin/cluster-health conn {:index [\"index1\",\"index2\"]})
  (admin/cluster-health conn {:index \"index1\" :pretty true :level \"indices\"})
  ```"
  ([^Connection conn] (cluster-health conn nil))
  ([^Connection conn opts]
   (rest/get conn (rest/cluster-health-url conn
                                           (join-names (:index opts)))
             {:query-params (dissoc opts :index)})))

(defn cluster-state
  "see <http://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-state.html>

  Examples:

  ```clojure
  (require '[clojurewerkz.elastisch.rest.admin :as admin])

  (admin/cluster-state conn)
  ```"
  ([^Connection conn] (cluster-state conn nil))
  ([^Connection conn opts]
   (rest/get conn (rest/cluster-state-url conn) {:query-params opts})))


(defn nodes-stats
  "see <http://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html>

  Examples:

  ```clojure
  (require '[clojurewerkz.elastisch.rest.admin :as admin])

  (admin/nodes-stats conn)
  (admin/nodes-stats conn {:nodes [\"10.0.0.1\", \"10.0.0.2\"] :attributes [\"os\" \"plugins\"]})
  ```"
  ([^Connection conn] (nodes-stats conn nil))
  ([^Connection conn opts]
   (rest/get conn (rest/cluster-nodes-stats-url conn
                                                (join-names (get opts :nodes "_all"))
                                                (join-names (get opts :attributes "_all"))))))

(defn nodes-info
  "see <http://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-nodes-info.html>

  Examples:

  ```clojure
  (require '[clojurewerkz.elastisch.rest.admin :as admin])

  (admin/nodes-info conn)
  (admin/nodes-info conn {:nodes [\"10.0.0.1\", \"10.0.0.2\"] :attributes [\"os\" \"plugins\"]})
  ```"
  ([^Connection conn] (nodes-info conn nil))
  ([^Connection conn opts]
   (rest/get conn (rest/cluster-nodes-info-url conn
                                               (join-names (get opts :nodes "_all"))
                                               (join-names (get opts :attributes "_all"))))))


(defn register-snapshot-repository
  ([^Connection conn ^String name] (register-snapshot-repository conn name nil))
  ([^Connection conn ^String name opts]
   (rest/put conn (rest/snapshot-repository-registration-url conn
                                                             name)
             {:body opts})))


(defn take-snapshot
  ([^Connection conn ^String repo ^String name] (take-snapshot conn repo name nil))
  ([^Connection conn ^String repo ^String name opts]
   (rest/put conn (rest/snapshot-url conn
                                     repo name)
             {:body opts :query-params (select-keys opts [:wait-for-completion?])})))

(defn restore-snapshot
  ([^Connection conn ^String repo ^String name] (restore-snapshot conn repo name nil))
  ([^Connection conn ^String repo ^String name opts]
   (rest/post conn (rest/restore-snapshot-url conn
                                              repo name)
              {:body opts
               :query-params (select-keys opts [:wait-for-completion?])})))

(defn delete-snapshot
  ([^Connection conn ^String repo ^String name] (delete-snapshot conn repo name nil))
  ([^Connection conn ^String repo ^String name opts]
   (rest/delete conn (rest/snapshot-url conn
                                        repo name)
                {:query-params (select-keys opts [:wait-for-completion?])})))
