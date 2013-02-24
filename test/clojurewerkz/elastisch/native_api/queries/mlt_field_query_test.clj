(ns clojurewerkz.elastisch.native-api.queries.mlt-field-query-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response
        [clj-time.core :only [months ago now from-now]]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)


;;
;; mlt query
;;

(deftest ^{:query true :native true} test-more-like-this-field-query
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/mlt-field :tags {:tags {:like_text "technology, opensource, search, full-text search, distributed, software, lucene"
                                                                                           :min_term_freq 1 :min_doc_freq 1}}))]
    (is (= 2 (total-hits response)))
    (is (= #{"1" "2"} (ids-from response)))))
