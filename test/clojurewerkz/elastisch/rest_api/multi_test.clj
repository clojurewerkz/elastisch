;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.multi-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.multi :as multi]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-multi-search
    (let [res1 (doc/search conn "people"   "person"  {:query (q/match-all) :size 1})
          res2 (doc/search conn "articles" "article" {:query (q/match-all) :size 1})
          multires (multi/search conn [{:index "people"
                                        :type "person"}  {:query (q/match-all) :size 1}
                                       {:index "articles"
                                        :type "article"} {:query (q/match-all) :size 1}
                                       {:index "tweets"
                                        :type "tweet"}   {:query (q/match-all) :size 1}])]
      (is (= 3 (count multires)))
      (is (= (-> res1 hits-from first source-from)
             (-> multires first hits-from first source-from)))
      (is (= (-> res2 hits-from first source-from)
             (-> multires second hits-from first source-from)))))

  (deftest ^{:rest true} test-multi-with-index-and-type
    (let [res1 (doc/search conn "people" "person" {:query (q/term :planet "earth")})
          res2 (doc/search conn "people" "person" {:query (q/term :first-name "mary")})
          multires (multi/search-with-index-and-type conn
                                                     "people" "person"
                                                     [{} {:query (q/term :planet "earth")}
                                                      {} {:query (q/term :first-name "mary")}
                                                      {} {:query (q/match-all)}])]
      (is (= 3 (count multires)))
      (is (= (res1 :hits) (-> multires first :hits)))
      (is (= (res2 :hits) (-> multires second :hits))))))
