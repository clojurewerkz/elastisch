(ns clojurewerkz.elastisch.rest-api.queries.text-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-tweets-index)

;;
;; text query
;;

(deftest ^{:query true} test-basic-text-query-over-a-nested-field
  (let [response (doc/search "articles" "article" :query (q/text "latest-edit.author" "Thorwald"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "2" (-> hits first :_id)))))


(deftest ^{:query true} test-basic-text-query-over-a-text-field-analyzed-with-standard-analyzer
  (let [response (doc/search "tweets" "tweet" :query (q/text :text "getting on the team"))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 1 (total-hits response)))
    (is (= "3" (-> hits first :_id)))))
