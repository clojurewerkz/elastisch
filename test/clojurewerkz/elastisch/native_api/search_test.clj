;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.search-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native          :as es]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all])
  (:import java.util.UUID))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true} test-search-with-multiple-versions-of-a-document-matching-a-query
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

  (deftest ^{:native true} test-search-query-with-basic-filtering
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query  (q/match-all)
                                               :filter {:term {:username "esmary"}}}))]
      (is (= 1 (count hits)))))

  (deftest ^{:native true} test-basic-sorting-over-string-field-with-implicit-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort  "title"})
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Apache Lucene" (-> hits first source-from :title)))
      (is (= "Nueva York" (-> hits last source-from :title)))))
  
  (deftest ^{:native true} test-basic-sorting-over-string-field-with-desc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort (array-map "title" "desc")})
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits first source-from :title)))
      (is (= "Austin" (-> hits last source-from :title)))))

  (deftest ^{:native true} test-search-with-source-enabled
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort (array-map "title" "desc")
                                                                 :fields ["title"]
                                                                 :_source true
                                                                 })
          hit         (first (hits-from response))]
    (is (some? (source-from hit))
    (is (= (get-in hit [:_fields  :title])  ["Nueva York"])))))

  (deftest ^{:native true} test-search-with-source-disabled

    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort (array-map "title" "desc")
                                                                 :fields ["title"]
                                                                 :_source false
                                                                 })
          hit         (first (hits-from response))]
    (is (nil? (source-from hit))
    (is (= (get-in hit [:_fields  :title])  ["Nueva York"])))))

  (deftest ^{:native true} test-basic-sorting-over-string-field-with-asc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type {:query (q/match-all)
                                                                 :sort (array-map "title" "asc")})
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits last source-from :title)))
      (is (= "Apache Lucene" (-> hits first source-from :title)))))

  (deftest test-searching-returning-fields
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :fields ["title"]})
          hits         (hits-from response)
          title-fields (remove nil? (map #(get-in % [:_fields :title]) hits))
          title-source (remove nil? (map #(-> % source-from :title) hits))]
      (is (= 4 (total-hits response)))
      (is (= 4 (count title-fields)))
      (is (= 0 (count title-source)))))

  (deftest test-searching-returning-fields-and-source
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :fields ["title" "_source"]})
          hits         (hits-from response)
          title-fields (remove nil? (map #(get-in % [:_fields :title]) hits))
          title-source (remove nil? (map #(-> % source-from :title) hits))]
      (is (= 4 (total-hits response)))
      (is (= 4 (count title-fields)))
      (is (= 4 (count title-source)))))

  (deftest ^{:native true} test-search-query-with-source-filtering-via-include
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query   (q/match-all)
                                               :sort    {"first-name" "asc"}
                                               :_source ["first-name" "age"]}))]
      (is (= 4 (count hits)))
      (is (= {:first-name "Tony" :age 29} (-> hits last source-from)))))

  (deftest ^{:native true} test-search-query-with-source-filtering-via-exclude
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

  (deftest ^{:native true} test-sorting-on-unmapped-field
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              {:query   (q/match-all)
                                               :sort    (q/sort "surname"
                                                                {:order "asc"
                                                                 :ignore-unmapped true})}))]
      (is (= 4 (count hits)))))

  (deftest ^{:native true} search-using-template-with-results
  (doc/create-search-template conn "test-template1" fx/test-template1)
  (doc/create conn "tweets" "tweet" fx/tweet1)
    (let [result (map :source (hits-from (doc/search conn "tweets" "tweet"
                             {:template {:id "test-template1"}
                              :params {:username "clojurewerkz"}})))]
  (is (= 1 (count result)))))



  (deftest ^{:native true} search-using-template-without-results
  (doc/create-search-template conn "test-template1" fx/test-template1)
  (doc/create conn "tweets" "tweet" fx/tweet1)
    (let [result (map :source (hits-from (doc/search conn "tweets" "tweet"
                             {:template {:id "test-template1"}
                              :params {:username "returns nothing"}})))]
  (is (empty?  result)))))
