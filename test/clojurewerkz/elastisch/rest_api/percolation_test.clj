;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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

(let [conn (rest/connect)]
  (deftest ^{:rest true :percolation true} test-percolation-case-1
    (let [index-name   "articles"
          percolator   "article"
          result1      (pcl/register-query conn index-name percolator {:query {:term {:title "search"}}})
          result2      (pcl/percolate conn index-name percolator {:doc {:title "You know, for search"}})]
      (is (= [{:_index "articles" :_id "article"}] (matches-from result2)))
      (pcl/unregister-query conn index-name percolator)))

  (deftest ^{:rest true :percolation true} test-percolation-existing-doc
    (let [index-name   "articles"
          percolator   "article"
          response     (doc/put conn index-name percolator "123" {:title "You know, for search"})
          result1      (pcl/register-query conn index-name percolator {:query {:term {:title "search"}}})
          result2      (pcl/percolate-existing conn index-name percolator "123")]
      (is (= [{:_index "articles" :_id "article"}] (matches-from result2)))
      (pcl/unregister-query conn index-name percolator))))
