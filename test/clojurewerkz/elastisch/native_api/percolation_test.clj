(ns clojurewerkz.elastisch.native-api.percolation-test
  (:require [clojurewerkz.elastisch.native             :as es]
            [clojurewerkz.elastisch.native.document    :as doc]
            [clojurewerkz.elastisch.native.index       :as idx]
            [clojurewerkz.elastisch.query              :as q]
            [clojurewerkz.elastisch.fixtures           :as fx]
            [clojurewerkz.elastisch.native.percolation :as pcl]
            [clojurewerkz.elastisch.test.helpers       :as th]
            [clojure.walk :as wlk])
  (:use clojure.test clojurewerkz.elastisch.native.response)
  (:import java.util.Map
           org.elasticsearch.client.Client))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(deftest ^{:native true :percolation true} test-percolation-case-1
  (let [index-name   "test"
        query-name   "kuku"
        _            (idx/create index-name :settings {"index.number_of_shards" 1})
        result1      (pcl/register-query index-name query-name :query {:term {:title "search"}})
        result2      (pcl/percolate index-name "type1" :doc {:title "You know, for search"})]
    (is (ok? result1))
    (is (ok? result2))
    (is (= [query-name] (matches-from result2)))
    (is (ok? (pcl/unregister-query index-name query-name)))))
