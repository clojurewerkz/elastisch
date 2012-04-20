(ns clojurewerkz.elastisch.queries.query-string-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.document :as doc]
            [clojurewerkz.elastisch.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.response
        [clj-time.core :only [months ago now from-now]]))

(def ^{:const true} index-name "articles")
(def ^{:const true} index-type "article")

(defn prepopulate-index
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
;; mlt query
;;

(deftest test-query-string-query
  (let [response (doc/search index-name index-type :query (q/query-string :query "Austin" :default_field "title"))]
    (is (= 2 (total-hits response)))
    (is (= #{"4"} (ids-from response)))))
