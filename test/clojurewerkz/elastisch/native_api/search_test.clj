;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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
        (let [result (doc/search conn index-name mapping-type :query (q/term :biography "say"))]
          (is (= 1 (total-hits result)))))))

  (deftest ^{:native true} test-search-query-with-basic-filtering
    (let [index-name   "people"
          mapping-type "person"
          hits         (hits-from (doc/search conn index-name mapping-type
                                              :query  (q/match-all)
                                              :filter {:term {:username "esmary"}}))]
      (is (= 4 (count hits)))))

  #_ (deftest ^{:query true :native true} test-query-validation
       (let [index-name   "articles"
             response     (doc/validate-query conn index-name (q/field "latest-edit.author" "Thorwald") :explain true)]
         (is (valid? response))))

  (deftest test-basic-sorting-over-string-field-with-desc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type :query (q/match-all)
                                   :sort (array-map "title" "desc"))
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits first :_source :title)))
      (is (= "Austin" (-> hits last :_source :title)))))

  (deftest test-basic-sorting-over-string-field-with-asc-order
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type :query (q/match-all)
                                   :sort (array-map "title" "asc"))
          hits         (hits-from response)]
      (is (= 4 (total-hits response)))
      (is (= "Nueva York" (-> hits last :_source :title)))
      (is (= "Apache Lucene" (-> hits first :_source :title)))))

  (deftest test-searching-returning-fields
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   :query (q/match-all)
                                   :fields ["title"])
          hits         (hits-from response)
          title-fields (remove nil? (map #(get-in % [:_fields :title]) hits))
          title-source (remove nil? (map #(get-in % [:_source :title]) hits))]
      (is (= 4 (total-hits response)))
      (is (= 4 (count title-fields)))
      (is (= 0 (count title-source)))))

  (deftest test-searching-returning-fields-and-source
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   :query (q/match-all)
                                   :fields ["title" "_source"])
          hits         (hits-from response)
          title-fields (remove nil? (map #(get-in % [:_fields :title]) hits))
          title-source (remove nil? (map #(get-in % [:_source :title]) hits))]
      (is (= 4 (total-hits response)))
      (is (= 4 (count title-fields)))
      (is (= 4 (count title-source))))))
