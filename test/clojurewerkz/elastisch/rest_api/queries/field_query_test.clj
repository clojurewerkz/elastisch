(ns clojurewerkz.elastisch.rest-api.queries.field-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

;;
;; field query
;;

(deftest ^{:query true} test-basic-field-query
  (let [index-name   "articles"
        mapping-type "article"
        response (doc/search index-name mapping-type :query (q/field "latest-edit.author" "Thorwald"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "2" (-> hits first :_id)))))
