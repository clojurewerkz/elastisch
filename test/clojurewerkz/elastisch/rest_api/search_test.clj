;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.search-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index :as idx]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all])
  (:import java.util.UUID))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true} test-search-with-multiple-versions-of-a-document-matching-a-query
    (testing "that only one version is stored (versions are just for MVCC, that is, conflict resolution)"
      (let [index-name   "people"
            mapping-type "person"
            id           (str (UUID/randomUUID))]
        (dotimes [n 5]
          (doc/put conn index-name mapping-type id {:username   "esrob"
                                                    :first-name "Robert"
                                                    :last-name  "White"
                                                    :title      "Chief Naysayer"
                                                    :biography  "Just says no, period"
                                                    :planet     "Earth"
                                                    :age 42}))
        (idx/refresh conn index-name)
        (let [result (doc/search conn index-name mapping-type {:query (q/term :biography "say")})]
          (is (= 1 (total-hits result)))))))

  (deftest ^{:rest true} test-search-query-with-basic-filtering
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query  (q/match-all)
                                               :filter {:term {:username "esmary"}}}))]
      (is (= 1 (count hits)))
      (is (= "Lindey" (-> hits first source-from :last-name)))))

  (deftest ^{:query true} test-query-validation
    (let [index-name   "articles"
          response     (doc/validate-query conn index-name (q/term "latest-edit.author" "Thorwald") {:explain true})]
      (is (valid? response))))

  (deftest ^{:rest true} test-basic-sorting-over-string-field-with-desc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort {"title" "desc"}})
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits first source-from :title)))
      (is (= "Austin" (-> hits last source-from :title)))))

  (deftest ^{:rest true} test-basic-sorting-over-string-field-with-asc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort {"title" "asc"}})
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits last source-from :title)))
      (is (= "Apache Lucene" (-> hits first source-from :title)))))

  (deftest ^{:rest true} test-search-query-with-source-filtering-via-include
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query   (q/match-all)
                                               :sort    {"first-name" "asc"}
                                               :_source ["first-name" "age"]}))]
      (is (= 4 (count hits)))
      (is (= {:first-name "Tony" :age 29} (-> hits last source-from)))))

  (deftest ^{:rest true} test-search-query-with-source-filtering-via-exclude
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query   (q/match-all)
                                               :sort    {"first-name" "asc"}
                                               :_source {"exclude" ["title" "country"
                                                                    "planet" "biography"
                                                                    "last-name" "username"]}}))]
      (is (= 4 (count hits)))
      (is (= #{:first-name :age :signed_up_at} (set (keys (-> hits last source-from)))))))

  (deftest ^{:rest true} test-sorting-on-unmapped-field
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query   (q/match-all)
                                               :sort    (q/sort "surname"
                                                                {:order "asc"
                                                                 :ignore-unmapped true})}))]
      (is (= 4 (count hits)))))

  (deftest ^{:rest true} test-ignore-unavailable
    (let [index-name   "people"
          missing-index-name "foo"
          mapping-type "person"]
      (is (= 4 (count (hits-from (doc/search conn [index-name missing-index-name] 
                                             mapping-type
                                             {:query   (q/match-all)
                                              :ignore_unavailable true})))))
      (is (thrown? Exception 
                   (doc/search conn [index-name missing-index-name]
                               mapping-type
                               {:query   (q/match-all)}))))))
