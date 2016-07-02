;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.query-test
  (:require [clojurewerkz.elastisch.query :as query]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.test.helpers :refer [ci?]]))


(deftest term-query-test
  (let [expected {:term {:foo "bar"}}]
    (is (= expected (query/term :foo "bar"))))

  (let [expected {:term {:foo :bar} :minimum_match 2}]
    (is (= expected (query/term :foo :bar {:minimum_match 2})))))

(deftest terms-query-test
  (let [expected {:terms {:foo [:bar :baz]}}]
    (is (= expected (query/term :foo [:bar :baz])))))

(deftest range-query-test
  (is (= {:range {:foo {:gt 5 :lt 10 :include_upper false :include_lower false}}}
         (query/range :foo {:gt 5 :lt 10 :include_upper false :include_lower false}))))

(deftest bool-query-test
  (let [must                         {:term {:user  "kimchy"}}
        should                       [{:term  {:tag  "wow"}} {:term  {:tag  "elasticsearch"}}]
        must-not                     {:range {:age {:from  10 :to  20}}}
        minimum-number-should-match  1
        boost                        1.0

        result                       (query/bool
                                      {:must (query/term :user "kimchy")
                                       :must_not (query/range :age {:from 10 :to 20})
                                       :should [(query/term :tag "wow") (query/term :tag "elasticsearch")]
                                       :minimum_number_should_match 1
                                       :boost 1.0})
        bool                         (:bool result)]
    (are [actual expected] (= actual expected)
         must (:must bool)
         should (:should bool)
         must-not (:must_not bool)
         minimum-number-should-match (:minimum_number_should_match bool)
         boost  (:boost bool))))

(deftest boosting-query-test
  (let [positive       {:term {:field1 "value1"}}
        negative       {:term {:field2 "value2"}}
        positive-boost 1.0
        negative-boost 0.2
        result         (query/boosting
                        {:positive (query/term :field1 "value1")
                         :negative (query/term :field2 "value2")
                         :positive_boost 1.0
                         :negative_boost 0.2})
        boosting       (:boosting result)]

    (are [actual expected] (= actual expected)
         positive       (:positive boosting)
         negative       (:negative boosting)
         positive-boost (:positive_boost boosting)
         negative-boost (:negative_boost boosting))))

(deftest type-query-test
  (is (=  {:type {:value "my_type"}}
          (query/type "my_type"))))

(deftest ids-query-test
  (is (=  {:ids {:type "my_type" :values  ["1" "4" "100"]}}
          (query/ids "my_type" ["1" "4" "100"]))))

(deftest constant-score-query-test
  (let [query   {:terms {:foo [:bar :baz]}}
        boost   2.0
        result  (query/constant-score
                 {:query   (query/term :foo [:bar :baz])
                  :boost   boost})
        constant-score (:constant_score result)]
    (are [actual expected] (= actual expected)
         query    (:query constant-score)
         boost    (:boost constant-score))))

(deftest dis-max-query-test
  (let [query1   {:term {:age 24}}
        query2   {:term {:age 35}}
        boost       2.0
        tie-breaker 0.7
        result  (query/dis-max
                 {:queries     [query1 query2]
                  :boost       boost
                  :tie_breaker tie-breaker})
        dis-max  (:dis_max result)]
    (are [actual expected] (= actual expected)
         query1   (first  (:queries dis-max))
         query2   (second (:queries dis-max))
         boost       (:boost dis-max)
         tie-breaker (:tie_breaker dis-max))))

(when-not (ci?)
  (deftest query-string-test
    (let [raw-query "+ - && & || | ! ( ) { } [ ] ^ \" ~ * ? : \\"
          escaped-query "\\+ \\- \\&& & \\|| | \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\"
          result-with-default-escaping (query/query-string {:query raw-query})
          result-with-explicit-escape-fn (query/query-string {:query raw-query :escape-with identity})]
      (is (= escaped-query
             (get-in result-with-default-escaping [:query_string :query])))
      (is (= raw-query
             (get-in result-with-explicit-escape-fn [:query_string :query]))))))
