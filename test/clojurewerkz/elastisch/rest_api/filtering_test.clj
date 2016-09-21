;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.filtering-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-term-filtering
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query  (q/match-all)
                                               :filter {:term {:username "esmary"}}}))]
      (is (= 1 (count hits)))
      (is (= "Lindey" (-> hits first source-from :last-name)))))

  (deftest ^{:rest true} test-range-filtering
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query  (q/match-all)
                                               :filter {:range {:age {:from 26 :to 30}}}}))]
      (is (= 2 (count hits)))
      (is (#{28 29} (-> hits first source-from :age))))))
