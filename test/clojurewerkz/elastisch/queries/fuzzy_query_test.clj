(ns clojurewerkz.elastisch.queries.fuzzy-query-test
  (:require [clojurewerkz.elastisch.document      :as doc]
            [clojurewerkz.elastisch.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response))


(def ^{:const true} index-name "articles")
(def ^{:const true} index-type "article")

(defn- prepopulate-index
  [f]
  (idx/create index-name :mappings fx/articles-mapping)

  (doc/put index-name index-type "1" fx/article-on-elasticsearch)
  (doc/put index-name index-type "2" fx/article-on-lucene)
  (doc/put index-name index-type "3" fx/article-on-nueva-york)
  (doc/put index-name index-type "4" fx/article-on-austin)
  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)

;;
;; flt query
;;

(deftest ^{:query true} test-basic-fuzzy-query-with-string-fields
  (let [response (doc/search index-name index-type :query (q/fuzzy :title "Nueva"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= #{"3"} (ids-from response)))))

(deftest ^{:query true} test-basic-fuzzy-query-with-numeric-fields
  (let [response (doc/search index-name index-type :query (q/fuzzy :number-of-edits {:value 13000 :min_similarity 3}))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

;; TODO: ES 0.19.2 spills 500s complaining about java.lang.IllegalArgumentException: minimumSimilarity >= 1
#_ (deftest ^{:query true} test-basic-fuzzy-query-with-date-fields
     (let [response (doc/search index-name index-type :query (q/fuzzy "last-edit.date" {:value "2012-03-25T12:00:00" :min_similarity "1d"}))
           hits     (hits-from response)]
       (is (any-hits? response))
       (is (= 1 (total-hits response)))
       (is (= #{"4"} (ids-from response)))))
