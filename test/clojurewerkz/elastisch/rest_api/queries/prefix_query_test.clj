(ns clojurewerkz.elastisch.rest-api.queries.prefix-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

;;
;; prefix query
;;

(deftest ^{:rest true :query true} test-basic-prefix-query
  (let [index-name   "people"
        mapping-type "person"
        response     (doc/search index-name mapping-type :query (q/prefix :username "esj"))
        hits         (hits-from response)]
    (is (any-hits? response))
    (is (= 2 (total-hits response)))
    (is (= #{"1" "3"} (set (map :_id hits))))))

(deftest ^{:rest true :query true} test-full-word-prefix-query-over-a-text-field-analyzed-with-the-standard-analyzer
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/prefix :text "why"))
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "4" (-> hits first :_id)))))

(deftest ^{:rest true :query true} test-partial-prefix-query-over-a-text-field
  (let [index-name   "tweets"
        mapping-type "tweet"
        response (doc/search index-name mapping-type :query (q/prefix :text "congr"))
        hits     (hits-from response)]
    (is (= 1 (total-hits response)))
    (is (= "3" (-> hits first :_id)))))
