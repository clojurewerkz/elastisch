(ns clojurewerkz.elastisch.rest-api.cluster-state-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(deftest ^{:rest true} cluster-state
  (is (= (into #{} (keys (admin/cluster-state)))
         #{:cluster_name
           :master_node
           :blocks
           :nodes
          :metadata
          :routing_table
           :routing_nodes
           :allocations}))
  (is (= (into #{} (keys (admin/cluster-state :filter_nodes true)))
         #{:cluster_name
           :blocks
           :metadata
           :routing_table
           :routing_nodes
           :allocations})))
