(ns clojurewerkz.elastisch.rest.admin
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]]))

;;
;; API
;;

(defn cluster-health
  "see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/cluster-health.html

   Examples:

   (require '[clojurewerkz.elastisch.rest :as rest])

   (rest/cluster-health)
   (rest/cluster-health :index \"index1\")
   (rest/cluster-health :index [\"index1\",\"index2\"])
   (rest/cluster-health :index \"index1\" :pretty true :level \"indices\")"
  [& {:as params}] 
  (rest/get (rest/cluster-health-url (join-names (:index params)))
            :query-params (dissoc params :index)))

