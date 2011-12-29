(ns elastisch.test.query
  (:require [elastisch.query :as query])
  (:use [clojure.test]))


(deftest term-query-test
  (let [expected { :term { :foo :bar } }]
    (is (= expected (query/term :foo :bar))))

  (let [expected { :terms { :foo [ :bar :baz ] } }]
    (is (= expected (query/term :foo [:bar :baz]))))

  (let [expected { :term { :foo :bar } :minimum_match 2 }]
    (is (= expected (query/term :foo :bar :minimum-match 2)))))

(deftest range-query-test
  (is (= { :range { :foo { :gt 5 :lt 10 :include_upper false :include_lower false } } }
         (query/range :foo :gt 5 :lt 10 :include-upper false :include-lower false))))