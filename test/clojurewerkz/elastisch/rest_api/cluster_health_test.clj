(ns clojurewerkz.elastisch.rest-api.cluster-health-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(deftest ^{:rest true} cluster-health
  (is (= {:number_of_nodes 1,
          :status "yellow",
          :relocating_shards 0,
          :unassigned_shards 10,
          :active_shards 10,
          :timed_out false,
          :active_primary_shards 10,
          :cluster_name "elasticsearch",
          :initializing_shards 0,
          :number_of_data_nodes 1}
         (admin/cluster-health)))
  (is (= {:number_of_nodes 1,
          :status "yellow",
          :relocating_shards 0,
          :unassigned_shards 5,
          :active_shards 5,
          :timed_out false,
          :active_primary_shards 5,
          :cluster_name "elasticsearch",
          :initializing_shards 0,
          :number_of_data_nodes 1}
         (admin/cluster-health :index "tweets")))
  (is (= {:number_of_nodes 1,
          :status "yellow",
          :relocating_shards 0,
          :unassigned_shards 10,
          :active_shards 10,
          :timed_out false,
          :active_primary_shards 10,
          :cluster_name "elasticsearch",
          :initializing_shards 0,
          :number_of_data_nodes 1}
         (admin/cluster-health :index ["tweets" "people"])))
  (is (= {:number_of_nodes 1,
          :status "yellow",
          :relocating_shards 0,
          :unassigned_shards 5,
          :active_shards 5,
          :timed_out false,
          :active_primary_shards 5,
          :cluster_name "elasticsearch",
          :initializing_shards 0,
          :number_of_data_nodes 1,
          :indices
          {:tweets
           {:status "yellow",
            :relocating_shards 0,
            :number_of_replicas 1,
            :unassigned_shards 5,
            :active_shards 5,
            :number_of_shards 5,
            :active_primary_shards 5,
            :shards
            {:0
             {:status "yellow",
              :primary_active true,
              :active_shards 1,
              :relocating_shards 0,
              :initializing_shards 0,
              :unassigned_shards 1},
             :1
             {:status "yellow",
              :primary_active true,
              :active_shards 1,
              :relocating_shards 0,
              :initializing_shards 0,
              :unassigned_shards 1},
             :2
             {:status "yellow",
              :primary_active true,
              :active_shards 1,
              :relocating_shards 0,
              :initializing_shards 0,
              :unassigned_shards 1},
             :3
             {:status "yellow",
              :primary_active true,
              :active_shards 1,
              :relocating_shards 0,
              :initializing_shards 0,
              :unassigned_shards 1},
             :4
             {:status "yellow",
              :primary_active true,
              :active_shards 1,
              :relocating_shards 0,
              :initializing_shards 0,
              :unassigned_shards 1}},
            :initializing_shards 0}}}
         (admin/cluster-health :index ["tweets"] :level "shards"))))
