(ns clojurewerkz.elastisch.queries.query-string-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response
        [clj-time.core :only [months ago now from-now]]))

(def ^{:const true} index-name "articles")
(def ^{:const true} mapping-type "article")

(defn prepopulate-index
  [f]
  (idx/create index-name :mappings fx/articles-mapping)
  (doc/put index-name mapping-type "1" fx/article-on-elasticsearch)
  (doc/put index-name mapping-type "2" fx/article-on-lucene)
  (doc/put index-name mapping-type "3" fx/article-on-nueva-york)
  (doc/put index-name mapping-type "4" fx/article-on-austin)

  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)


;;
;; query string query
;;

(deftest ^{:query true} test-query-string-query
  (let [response (doc/search index-name mapping-type :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true} test-query-string-query-across-all-mapping-types
  (let [response (doc/search-all-types index-name :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))

(deftest ^{:query true} test-query-string-query-across-all-mapping-types
  (let [response (doc/search-all-indexes-and-types index-name :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 1 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))
