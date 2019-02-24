;; Copyright (c) 2011-2019 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.mlt-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(let [conn (rest/connect)]
  (deftest ^{:query true :rest true} test-more-like-this-query
    (let [index-name   "articles"
          mapping-type "article"
          response (doc/search conn index-name mapping-type {:query (q/mlt {:like_text "technology, opensource, search, full-text search, distributed, software, lucene"
                                                                            :fields ["tags"] :min_term_freq 1 :min_doc_freq 1})})]
      (is (= 2 (total-hits response)))
      (is (= #{"1" "2"} (ids-from response))))))
