(ns clojurewerkz.elastisch.rest-api.nodes-stats-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-tweets-index)

(deftest nodes-stats
  (is (= #{:cluster_name :nodes}
         (into #{} (keys (admin/nodes-stats)))))
  (testing "node selection"
    (let [stats (admin/nodes-stats)
          node-id (first (keys (:nodes stats))) 
          node-name (get-in stats [:nodes node-id :name])]
      (is (empty? (:nodes (admin/nodes-stats :nodes "foo"))))
      (is (= 1 (count (:nodes (admin/nodes-stats :nodes (name node-id))))))
      (is (= 1 (count (:nodes (admin/nodes-stats :nodes (vector (name node-id)))))))
      (is (= 1 (count (:nodes (admin/nodes-stats :nodes node-name)))))))
  (testing "parameters"
    (is (not (= (admin/nodes-stats :indices true) (admin/nodes-stats :indices false))))
    (is (not (= (admin/nodes-stats :network true) (admin/nodes-stats :network false))))))
