;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.cluster-state-test
  (:require [clojurewerkz.elastisch.rest.admin :as admin]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(let [conn (rest/connect)]
  (deftest ^{:rest true} cluster-state
    (let [r (admin/cluster-state conn)]
      (is (:cluster_name r)))
    (let [r (admin/cluster-state conn {:filter_nodes true})]
      (is (:cluster_name r)))))
