(ns elastisch.test.query
  (:require [elastisch.query :as query])
  (:use [clojure.test]))


(deftest term-query-test
  (let [expected { :term { :foo "bar" } }]
    (is (= expected (query/term :foo "bar"))))

  (let [expected { :terms { :foo [ :bar :baz ] } }]
    (is (= expected (query/term :foo [:bar :baz]))))

  (let [expected { :term { :foo :bar } :minimum_match 2 }]
    (is (= expected (query/term :foo :bar :minimum-match 2)))))

(deftest range-query-test
  (is (= { :range { :foo { :gt 5 :lt 10 :include_upper false :include_lower false } } }
         (query/range :foo :gt 5 :lt 10 :include-upper false :include-lower false))))

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
                                      :must-not (query/range :age :from 10 :to 20)
                                      :should [ (query/term :tag "wow") (query/term :tag "elasticsearch") ]
                                      :minimum-number-should-match 1
                                      :boost 1.0)
        bool                         (:bool result)]
    (are [actual expected] (= actual expected)
         must (:must bool)
         should (:should bool)
         must-not (:must_not bool)
         minimum-number-should-match (:minimum_number_should_match bool)
         boost  (:boost bool))))

(deftest boosting
  (let [positive       { :term { :field1 "value1" } }
        negative       { :term { :field2 "value2" } }
        positive-boost 1.0
        negative-boost 0.2
        result         (query/boosting
                        :positive (query/term :field1 "value1")
                        :negative (query/term :field2 "value2")
                        :positive-boost 1.0
                        :negative-boost 0.2)
        boosting       (:boosting result)]

    (are [actual expected] (= actual expected)
         positive       (:positive boosting)
         negative       (:negative boosting)
         positive-boost (:positive_boost boosting)
         negative-boost (:negative_boost boosting))))
