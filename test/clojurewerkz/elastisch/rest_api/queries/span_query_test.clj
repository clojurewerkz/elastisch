;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.span-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

;; TODO: these return no results and ES test suite does not contain any meaningful tests for span queries.
;;       Lucene test suite does have SpanFirstQuery tests but they use custom matchers we need to figure out
;;       first. MK.
#_ (let [conn (rest/connect)]
     (deftest ^{:rest true :query true} test-span-first-query
       (let [response (doc/search conn "people" "person" :query (q/span-first :match {:span_term {:biography "eating"}} :end 5))
             hits     (hits-from response)]
         (is (any-hits? response))
         (is (= 1 (total-hits response)))
         (is (= #{"1"} (set (map :_id hits))))))

     (deftest ^{:rest true :query true} test-span-near-query
       (let [response (doc/search conn "articles" "article" :query (q/span-near :clauses [{:span_term {:summary "search"}}
                                                                                          {:span_term {:summary "documents"}}] :slop 5 :in_order true))
             hits     (hits-from response)]
         (is (any-hits? response))
         (is (= 1 (total-hits response)))
         (is (= #{"1"} (set (map :_id hits)))))))
