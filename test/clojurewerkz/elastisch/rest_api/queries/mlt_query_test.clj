(ns clojurewerkz.elastisch.rest-api.queries.mlt-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)


;;
;; mlt query
;;

(deftest ^{:query true :rest true} test-more-like-this-query
  (let [index-name   "articles"
        mapping-type "article"
        response (doc/search index-name mapping-type :query (q/mlt :like_text "technology, opensource, search, full-text search, distributed, software, lucene"
                                                                   :fields ["tags"] :min_term_freq 1 :min_doc_freq 1))]
    (is (= 2 (total-hits response)))
    (is (= #{"1" "2"} (ids-from response)))))
