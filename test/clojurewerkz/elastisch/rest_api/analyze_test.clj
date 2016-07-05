;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

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

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-analyze
    (is (= {:tokens
            [{:token "foo",
              :start_offset 0,
              :end_offset 3,
              :type "<ALPHANUM>",
              :position 0}
             {:token "bar",
              :start_offset 4,
              :end_offset 7,
              :type "<ALPHANUM>",
              :position 1}
             {:token "baz",
              :start_offset 8,
              :end_offset 11,
              :type "<ALPHANUM>",
              :position 2}]}
           (doc/analyze conn "foo bar-baz")))
    (is (= {:tokens
            [{:token "foo",
              :start_offset 0,
              :end_offset 3,
              :type "word",
              :position 0}
             {:token "bar-baz",
              :start_offset 4,
              :end_offset 11,
              :type "word",
              :position 1}]}
           (doc/analyze conn "foo bar-baz" {:analyzer "whitespace"}))))

  (deftest ^{:rest true} test-analyze-with-map-options
    (is (= {:tokens
            [{:token "foo",
              :start_offset 0,
              :end_offset 3,
              :type "<ALPHANUM>",
              :position 0}
             {:token "bar",
              :start_offset 4,
              :end_offset 7,
              :type "<ALPHANUM>",
              :position 1}
             {:token "baz",
              :start_offset 8,
              :end_offset 11,
              :type "<ALPHANUM>",
              :position 2}]}
           (doc/analyze conn "foo bar-baz")))
    (is (= {:tokens
            [{:token "foo",
              :start_offset 0,
              :end_offset 3,
              :type "word",
              :position 0}
             {:token "bar-baz",
              :start_offset 4,
              :end_offset 11,
              :type "word",
              :position 1}]}
           (doc/analyze conn "foo bar-baz" {:analyzer "whitespace"})))))

