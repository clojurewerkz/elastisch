;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest.admin
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import clojurewerkz.elastisch.rest.Connection))

;;
;; API
;;

(defn cluster-health
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-health.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/cluster-health conn)
   (admin/cluster-health conn :index \"index1\")
   (admin/cluster-health conn :index [\"index1\",\"index2\"])
   (admin/cluster-health conn :index \"index1\" :pretty true :level \"indices\")"
  [^Connection conn & args]
  (let [opts (ar/->opts args)]
    (rest/get (rest/cluster-health-url conn
                                       (join-names (:index opts)))
              :query-params (dissoc opts :index))))

(defn cluster-state
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-state.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/cluster-state conn)
"
  [^Connection conn & args]
  (rest/get (rest/cluster-state-url conn) :query-params (ar/->opts args)))


(defn nodes-stats
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/nodes-stats conn)
   (admin/nodes-stats conn :nodes [\"10.0.0.1\", \"10.0.0.2\"] :attributes [\"os\" \"plugins\"])
"
  [^Connection conn & args]
  (let [opts (ar/->opts args)]
    (rest/get (rest/cluster-nodes-stats-url conn
                                            (join-names (get opts :nodes "_all"))
                                            (join-names (get opts :attributes "_all"))))))

(defn nodes-info
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-info.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/nodes-info conn)
   (admin/nodes-info conn :nodes [\"10.0.0.1\", \"10.0.0.2\"] :attributes [\"os\" \"plugins\"])
"
  [^Connection conn & args]
  (let [opts (ar/->opts args)]
    (rest/get (rest/cluster-nodes-info-url conn
                                           (join-names (get opts :nodes "_all"))
                                           (join-names (get opts :attributes "_all"))))))


(defn register-snapshot-repository
  [^Connection conn ^String name & args]
  (rest/put (rest/snapshot-repository-registration-url conn
                                                       name) :body (ar/->opts args)))


(defn take-snapshot
  [^Connection conn ^String repo ^String name & args]
  (let [opts (ar/->opts args)]
    (rest/put (rest/snapshot-url conn
                                 repo name) :body opts :query-params (select-keys opts [:wait-for-completion?]))))

(defn restore-snapshot
  [^Connection conn ^String repo ^String name & args]
  (let [opts (ar/->opts args)]
    (rest/post (rest/restore-snapshot-url conn
                                          repo name)
             :body opts
             :query-params (select-keys opts [:wait-for-completion?]))))

(defn delete-snapshot
  [^Connection conn ^String repo ^String name & args]
  (let [opts (ar/->opts args)]
    (rest/delete (rest/snapshot-url conn
                                    repo name)
                 :query-params (select-keys opts [:wait-for-completion?]))))
