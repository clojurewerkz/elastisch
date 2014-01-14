(ns clojurewerkz.elastisch.escape-test
  (:require [clojurewerkz.elastisch.escape :as escape]
            [clojure.test :refer :all]))

(deftest escape-query-string-characters-test
  (testing "escapes Lucene special chars"
    (is (= "\\+ \\- \\&& & \\|| | \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\"
           (escape/escape-query-string-characters "+ - && & || | ! ( ) { } [ ] ^ \" ~ * ? : \\"))))

  (testing "does not escape non-special chars"
    (let [s "John %@= Doe"]
      (is (= s (escape/escape-query-string-characters s))))))
