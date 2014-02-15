(ns clojurewerkz.elastisch.rest-api.cluster-health-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(deftest ^{:rest true} cluster-health
  (let [r (admin/cluster-health)]
    (is (contains? r :number_of_nodes)))
  (let [r (admin/cluster-health :index "tweets")]
    (is (contains? r :number_of_nodes)))
  (let [r (admin/cluster-health :index ["tweets" "people"])]
    (is (contains? r :number_of_nodes)))
  (let [r (admin/cluster-health :index ["tweets"] :level "shards")]
    (is (contains? r :number_of_nodes))))
