(ns clojurewerkz.elastisch.rest-api.percolation-test
  (:require [clojurewerkz.elastisch.rest.document    :as doc]
            [clojurewerkz.elastisch.rest             :as rest]
            [clojurewerkz.elastisch.rest.index       :as idx]
            [clojurewerkz.elastisch.query            :as q]
            [clojurewerkz.elastisch.fixtures         :as fx]
            [clojurewerkz.elastisch.rest.percolation :as pcl]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(deftest ^{:rest true :percolation true} test-percolation-case-1
  (let [index-name   "articles"
        percolator   "article"
        result1      (pcl/register-query index-name percolator :query {:term {:title "search"}})
        result2      (pcl/percolate index-name percolator :doc {:title "You know, for search"})]
    (is (= [{:_index "articles", :_id "article"}] (matches-from result2)))
    (is (found? (pcl/unregister-query index-name percolator)))))
