;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.nodes-info-test
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-nodes-info
    (testing "basic info"
      (let [info (admin/nodes-info conn)]
        (is (:nodes info))
        (is (:cluster_name info))))
    (testing "node selection"
      (let [info (admin/nodes-info conn)
            node-id (first (keys (:nodes info)))
            node-name (get-in info [:nodes node-id :name])]
        (is (empty? (:nodes (admin/nodes-info conn {:nodes ["foo"]}))))
        (is (= 1 (count (:nodes (admin/nodes-info conn {:nodes (name node-id)})))))
        (is (= 1 (count (:nodes (admin/nodes-info conn {:nodes (vector (name node-id))})))))
        (is (= 1 (count (:nodes (admin/nodes-info conn {:nodes node-name})))))))
    (testing "parameters"
      (is (not (= (admin/nodes-info conn {:attributes ["plugins"]})
                  (admin/nodes-info conn {:attributes ["os"]})))))))
