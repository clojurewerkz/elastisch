(ns clojurewerkz.elastisch.native-api.queries.flt-field-query-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test clojurewerkz.elastisch.native.response))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

;;
;; flt query
;;

(deftest ^{:query true :native true} test-basic-flt-field-query
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/fuzzy-like-this-field :summary {:like_text "ciudad"}))
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"4" "3"} (ids-from response)))))
