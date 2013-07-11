(ns clojurewerkz.elastisch.rest-api.count-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test
        [clojurewerkz.elastisch.rest.response :only [count-from ok?]]))

(use-fixtures :each fx/reset-indexes)

;;
;; count
;;

(deftest test-count-with-the-default-query
  (let [index-name "people"
        index-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name index-type fx/person-jack)
    (doc/create index-name index-type fx/person-joe)
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         2 (doc/count index-name index-type))))

(deftest test-count-with-a-term-query
  (let [index-name "people"
        index-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name index-type fx/person-jack)
    (doc/create index-name index-type fx/person-joe)
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count index-name index-type (q/term :username "esjack"))
         1 (doc/count index-name index-type (q/term :username "esjoe"))
         0 (doc/count index-name index-type (q/term :username "esmary")))))


(deftest test-count-with-mixed-mappings
  (let [index-name "people"
        index-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name index-type fx/person-jack)
    (doc/create index-name index-type fx/person-joe)
    (doc/create index-name "altpeople" fx/person-jack)
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count index-name index-type (q/term :username "esjack"))
         1 (doc/count index-name "altpeople" (q/term :username "esjack"))
         0 (doc/count index-name "altpeople" (q/term :username "esjoe")))))

;;
;; Missing indices
;;

(deftest test-count-with-ignore-indices
  (let [index-name         "people"
        index-type         "person"
        missing-index-name "foo"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name index-type fx/person-jack)
    (doc/create index-name index-type fx/person-joe)
    (idx/refresh index-name)
    (is (not (ok? (doc/count [index-name missing-index-name] index-type))))
    (is (= 2
           (count-from
            (doc/count [index-name missing-index-name] index-type (q/match-all) :ignore_indices "missing"))))))
