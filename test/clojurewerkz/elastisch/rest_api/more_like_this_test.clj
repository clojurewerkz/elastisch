(ns clojurewerkz.elastisch.rest-api.more-like-this-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes)


;;
;; more-like-this
;;

(deftest test-more-like-this
  (let [index "articles"
        type  "article"]
    (idx/create index :mappings fx/articles-mapping)
    (doc/put index type "1" fx/article-on-elasticsearch)
    (doc/put index type "2" fx/article-on-lucene)
    (doc/put index type "3" fx/article-on-nueva-york)
    (doc/put index type "4" fx/article-on-austin)
    (idx/refresh index)
    (let [response (doc/more-like-this index type "2" :mlt_fields ["tags"] :min_term_freq 1 :min_doc_freq 1)]
      (is (= 1 (total-hits response)))
      (is (= fx/article-on-elasticsearch (-> (hits-from response) first :_source))))))
