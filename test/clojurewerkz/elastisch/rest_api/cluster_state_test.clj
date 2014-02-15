(ns clojurewerkz.elastisch.rest-api.cluster-state-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(deftest ^{:rest true} cluster-state
  (let [r (admin/cluster-state)]
    (is (:cluster_name r)))
  (let [r (admin/cluster-state :filter_nodes true)]
    (is (:cluster_name r))))
