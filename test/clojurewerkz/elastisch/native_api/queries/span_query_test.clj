(ns clojurewerkz.elastisch.native-api.queries.span-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

;;
;; Tests
;;

;; TODO: these return no results and ES test suite does not contain any meaningful tests for span queries.
;;       Lucene test suite does have SpanFirstQuery tests but they use custom matchers we need to figure out
;;       first. MK.
#_ (deftest ^{:query true} test-span-first-query
            (let [response (doc/search "people" "person" :query (q/span-first :match {:span_term {:biography "eating"}} :end 5))
                  hits     (hits-from response)]
              (is (any-hits? response))
              (is (= 1 (total-hits response)))
              (is (= #{"1"} (set (map :_id hits))))))

#_ (deftest ^{:query true} test-span-near-query
            (let [response (doc/search "articles" "article" :query (q/span-near :clauses [{:span_term {:summary "search"}}
                                                                                          {:span_term {:summary "documents"}}] :slop 5 :in_order true))
                  hits     (hits-from response)]
              (is (any-hits? response))
              (is (= 1 (total-hits response)))
              (is (= #{"1"} (set (map :_id hits))))))
