(ns clojurewerkz.elastisch.test.query
  (:require [clojurewerkz.elastisch.query :as query])
  (:use [clojure.test]))


(deftest term-query-test
  (let [expected { :term { :foo "bar" } }]
    (is (= expected (query/term :foo "bar"))))

  (let [expected { :terms { :foo [ :bar :baz ] } }]
    (is (= expected (query/term :foo [:bar :baz]))))

  (let [expected { :term { :foo :bar } :minimum_match 2 }]
    (is (= expected (query/term :foo :bar :minimum_match 2)))))

(deftest range-query-test
  (is (= { :range { :foo { :gt 5 :lt 10 :include_upper false :include_lower false } } }
         (query/range :foo :gt 5 :lt 10 :include_upper false :include_lower false))))

(deftest text-query-test
  (is (= { :text { :message { :query "this is a test" :analyzer "my_analyzer" :operator "and" } } }
         (query/text "this is a test" :operator "and" :analyzer "my_analyzer"))))

(deftest bool-query-test
  (let [must                         { :term { :user  "kimchy" } }
        should                       [ { :term  { :tag  "wow" } } { :term  { :tag  "elasticsearch" } } ]
        must-not                     { :range { :age { :from  10 :to  20 } } }
        minimum-number-should-match  1
        boost                        1.0

        result                       (query/bool
                                      :must (query/term :user "kimchy")
                                      :must_not (query/range :age :from 10 :to 20)
                                      :should [ (query/term :tag "wow") (query/term :tag "elasticsearch") ]
                                      :minimum_number_should_match 1
                                      :boost 1.0)
        bool                         (:bool result)]
    (are [actual expected] (= actual expected)
         must (:must bool)
         should (:should bool)
         must-not (:must_not bool)
         minimum-number-should-match (:minimum_number_should_match bool)
         boost  (:boost bool))))

(deftest boosting-query-test
  (let [positive       { :term { :field1 "value1" } }
        negative       { :term { :field2 "value2" } }
        positive-boost 1.0
        negative-boost 0.2
        result         (query/boosting
                        :positive (query/term :field1 "value1")
                        :negative (query/term :field2 "value2")
                        :positive_boost 1.0
                        :negative_boost 0.2)
        boosting       (:boosting result)]

    (are [actual expected] (= actual expected)
         positive       (:positive boosting)
         negative       (:negative boosting)
         positive-boost (:positive_boost boosting)
         negative-boost (:negative_boost boosting))))

(deftest ids-query-test
  (is (=  { :ids { :type "my_type" :values  ["1" "4" "100"] } }
          (query/ids "my_type" [ "1" "4" "100" ]))))

(deftest custom-score-query-test
  (let [query   { :terms { :foo [ :bar :baz ] } }
        params  { :param1  2 :param2  3.1 }
        script  "_score * doc['my_numeric_field'].value / pow(param1 param2)"
        result  (query/custom-score
                 :query   (query/term :foo [:bar :baz])
                 :params  { :param1  2 :param2  3.1 }
                 :script  "_score * doc['my_numeric_field'].value / pow(param1 param2)")
        custom-score (:custom_score result)]
    (are [actual expected] (= actual expected)
         query    (:query custom-score)
         params   (:params custom-score)
         script   (:script custom-score))))

(deftest constant-score-query-test
  (let [query   { :terms { :foo [ :bar :baz ] } }
        boost   2.0
        result  (query/constant-score
                 :query   (query/term :foo [:bar :baz])
                 :boost   boost)
        constant-score (:constant_score result)]
    (are [actual expected] (= actual expected)
         query    (:query constant-score)
         boost    (:boost constant-score))))

(deftest dis-max-query-test
  (let [query1   { :term { :age 24 } }
        query2   { :term { :age 35 } }
        boost       2.0
        tie-breaker 0.7
        result  (query/dis-max
                 :queries     [ query1, query2 ]
                 :boost       boost
                 :tie_breaker tie-breaker)
        dis-max  (:dis_max result)]
    (are [actual expected] (= actual expected)
         query1   (first  (:queries dis-max))
         query2   (second (:queries dis-max))
         boost       (:boost dis-max)
         tie-breaker (:tie_breaker dis-max))))
