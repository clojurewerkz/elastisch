(ns clojurewerkz.elastisch.rest-api.queries.span-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(defn prepopulate-index
  [f]
  (let [index-name "articles"
        mapping-type "article"]
    (idx/create index-name :mappings fx/articles-mapping)

    (doc/put index-name mapping-type "1" fx/article-on-elasticsearch)
    (doc/put index-name mapping-type "2" fx/article-on-lucene)
    (doc/put index-name mapping-type "3" fx/article-on-nueva-york)
    (doc/put index-name mapping-type "4" fx/article-on-austin)

    (idx/refresh index-name))


  (let [index-name "people"
        mapping-type "person"]
    (idx/create index-name :mappings fx/people-mapping)

    (doc/put index-name mapping-type "1" fx/person-jack)
    (doc/put index-name mapping-type "2" fx/person-mary)
    (doc/put index-name mapping-type "3" fx/person-joe)

    (idx/refresh index-name))

  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)

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
