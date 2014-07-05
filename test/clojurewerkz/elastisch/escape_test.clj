;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.escape-test
  (:require [clojurewerkz.elastisch.escape :as escape]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.test.helpers :refer [ci?]]))

(when-not (ci?)
  (deftest escape-query-string-characters-test
    (testing "escapes Lucene special chars"
      (is (= "\\+ \\- \\&& & \\|| | \\! \\( \\) \\{ \\} \\[ \\] \\^ \\\" \\~ \\* \\? \\: \\\\"
             (escape/escape-query-string-characters "+ - && & || | ! ( ) { } [ ] ^ \" ~ * ? : \\"))))

    (testing "does not escape non-special chars"
      (let [s "John %@= Doe"]
        (is (= s (escape/escape-query-string-characters s)))))))
