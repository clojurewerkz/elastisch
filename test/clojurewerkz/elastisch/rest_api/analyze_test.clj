(ns clojurewerkz.elastisch.rest-api.analyze-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest     :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all])
  (:import java.util.UUID))


;;
;; Analysis
;;

(deftest test-analyze
  (is (= {:tokens
          [{:token "foo",
            :start_offset 0,
            :end_offset 3,
            :type "<ALPHANUM>",
            :position 1}
           {:token "bar",
            :start_offset 4,
            :end_offset 7,
            :type "<ALPHANUM>",
            :position 2}
           {:token "baz",
            :start_offset 8,
            :end_offset 11,
            :type "<ALPHANUM>",
            :position 3}]}
         (doc/analyze "foo bar-baz")))
  (is (= {:tokens
          [{:token "foo",
            :start_offset 0,
            :end_offset 3,
            :type "word",
            :position 1}
           {:token "bar-baz",
            :start_offset 4,
            :end_offset 11,
            :type "word",
            :position 2}]}
         (doc/analyze "foo bar-baz" :analyzer "whitespace"))))
