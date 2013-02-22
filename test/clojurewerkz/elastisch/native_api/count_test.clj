(ns clojurewerkz.elastisch.native-api.count-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th])
  (:use clojure.test
        [clojurewerkz.elastisch.native.response :only [count-from]]))

(th/maybe-connect-native-client)
(use-fixtures :each fx/reset-indexes)

;;
;; count
;;

(deftest ^{:native true} test-count-with-the-default-query
  (let [index-name "people"
        index-type "person"]
    (idx/create index-name :mappings fx/people-mapping)
    (doc/create index-name index-type fx/person-jack)
    (doc/create index-name index-type fx/person-joe)
    (idx/refresh index-name)
    (are [c r] (is (= c (count-from r)))
         2 (doc/count index-name index-type))))

(deftest ^{:native true} test-count-with-a-term-query
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


(deftest ^{:native true} test-count-with-mixed-mappings
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
