;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.percolation-test
  (:require [clojurewerkz.elastisch.native             :as es]
            [clojurewerkz.elastisch.native.document    :as doc]
            [clojurewerkz.elastisch.native.index       :as idx]
            [clojurewerkz.elastisch.query              :as q]
            [clojurewerkz.elastisch.fixtures           :as fx]
            [clojurewerkz.elastisch.native.percolation :as pcl]
            [clojurewerkz.elastisch.test.helpers       :as th]
            [clojure.walk :as wlk]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all])
  (:import java.util.Map
           org.elasticsearch.client.Client))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(deftest ^{:native true :percolation true} test-percolation-case-1
  (let [index-name   "test"
        query-name   "kuku"
        _            (idx/create index-name :settings {"index.number_of_shards" 1})
        result1      (pcl/register-query index-name query-name :query {:term {:title "search"}})
        result2      (pcl/percolate index-name "type1" :doc {:title "You know, for search"} :refresh true)]
    (is (= [query-name] (matches-from result2)))
    (pcl/unregister-query index-name query-name)))
