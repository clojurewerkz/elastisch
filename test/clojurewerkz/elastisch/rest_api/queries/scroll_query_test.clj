(ns clojurewerkz.elastisch.rest-api.queries.scroll-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(deftest ^{:query true} test-basic-scan-query
  (let [index-name   "articles"
        mapping-type "article"
        response (doc/search
                  index-name
                  mapping-type
                  :query (q/query-string :query "*")
                  :search_type "scan"
                  :scroll "10m"
                  :size 1
                  )
        initial-hits         (hits-from response)
        scroll-id (:_scroll_id response)
        scan-response (doc/scan scroll-id :scroll "10m" )
        scan-hits (hits-from scan-response)
        ]
    (println "scroll_id" scroll-id)
    (is (any-hits? response))
    (is (= 4 (total-hits response)))
    ;; scan queries don't return any hits from the initial
    ;; search request
    (is (= 0 (count initial-hits)))
    (is (= 4 (total-hits scan-response)))
    (is (= 4 (count scan-hits)))))

(deftest ^{:query true} test-basic-scroll-query
  (let [index-name   "articles"
        mapping-type "article"
        response (doc/search
                  index-name
                  mapping-type
                  :query (q/query-string :query "*")
                  :search_type "query_then_fetch"
                  :scroll "1m"
                  :size 2
                  )
        initial-hits         (hits-from response)
        scroll-id (:_scroll_id response)
        scan-response (doc/scroll scroll-id :scroll "1m" )
        scan-hits (hits-from scan-response)
        ]
    (println "scroll_id" scroll-id)
    (is (any-hits? response))
    (is (= 4 (total-hits response)))
    (is (= 2 (count initial-hits)))
    (is (= 4 (total-hits scan-response)))
    (is (= 2 (count scan-hits)))))
