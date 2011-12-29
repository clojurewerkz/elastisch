(ns elastisch.test.utils
  (:require [elastisch.utils :as utils])
  (:use [clojure.test]
        [clojure.string]
        ))
;;
;; Utils
;;

(deftest join-names-test
  (is (= "name" (utils/join-names "name")))
  (is (= "name1,name2" (utils/join-names ["name1", "name2"]))))

(deftest join-hash-test
  (is (= "a=1&b=2&c=3&d=5&e=4" (utils/join-hash (sorted-map :a 1 :b 2 :c 3 :e 4 :d 5 )))))


