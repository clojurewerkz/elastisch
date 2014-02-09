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
            [clojurewerkz.elastisch.rest.utils :refer [join-names]]))

;;
;; API
;;

(defn cluster-health
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-health.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/cluster-health)
   (admin/cluster-health :index \"index1\")
   (admin/cluster-health :index [\"index1\",\"index2\"])
   (admin/cluster-health :index \"index1\" :pretty true :level \"indices\")"
  [& {:as params}] 
  (rest/get (rest/cluster-health-url (join-names (:index params)))
            :query-params (dissoc params :index)))

(defn cluster-state
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-state.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/cluster-state)
"
  [& {:as params}] 
  (rest/get (rest/cluster-state-url) :query-params params))


(defn nodes-stats
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/nodes-stats)
   (admin/nodes-stats :nodes [\"10.0.0.1\", \"10.0.0.2\"] :os true :process true)
"
  [& {:as params}] 
  (rest/get (rest/cluster-nodes-stats-url (join-names (:nodes params)))
            :query-params (dissoc params :nodes)))

(defn nodes-info
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-nodes-info.html

   Examples:

   (require '[clojurewerkz.elastisch.rest.admin :as admin])

   (admin/nodes-info)
   (admin/nodes-info :nodes [\"10.0.0.1\", \"10.0.0.2\"] :os true :process true)
"
  [& {:as params}] 
  (rest/get (rest/cluster-nodes-info-url (join-names (:nodes params)))
            :query-params (dissoc params :nodes)))


