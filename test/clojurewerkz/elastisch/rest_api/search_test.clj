(ns clojurewerkz.elastisch.rest-api.search-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest     :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all])
  (:import java.util.UUID))


(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

;;
;; Versioning
;;

(deftest test-search-with-multiple-versions-of-a-document-matching-a-query
  (testing "that only one version is stored (versions are just for MVCC, that is, conflict resolution)"
    (let [index-name   "people"
          mapping-type "person"
          id           (str (UUID/randomUUID))]
      (dotimes [n 5]
        (doc/put index-name mapping-type id {:username   "esrob"
                                             :first-name "Robert"
                                             :last-name  "White"
                                             :title      "Chief Naysayer"
                                             :biography  "Just says no, period"
                                             :planet     "Earth"
                                             :age 42}))
      (idx/refresh index-name)
      (let [result (doc/search index-name mapping-type :query (q/term :biography "say"))]
        (is (= 1 (total-hits result)))))))


;;
;; Filtering
;;

(deftest test-search-query-with-basic-filtering
  (let [index-name   "people"
        mapping-type "person"
        hits         (hits-from (doc/search index-name mapping-type
                                            :query  (q/match-all)
                                            :filter {:term {:username "esmary"}}))]
    (is (= 1 (count hits)))
    (is (= "Lindey" (-> hits first :_source :last-name)))))


;;
;; Query validation
;;

(deftest ^{:query true} test-query-validation
  (let [index-name   "articles"
        response     (doc/validate-query index-name (q/field "latest-edit.author" "Thorwald") :explain true)]
    (is (valid? response))))


;;
;; Sorting
;;

(deftest test-basic-sorting-over-string-field-with-desc-order
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/match-all)
                                 :sort {"title" "desc"})
        hits         (hits-from response)]
    (is (= 4 (total-hits response)))
    (is (= "Nueva York" (-> hits first :_source :title)))
    (is (= "Austin" (-> hits last :_source :title)))))

(deftest test-basic-sorting-over-string-field-with-asc-order
  (let [index-name   "articles"
        mapping-type "article"
        response     (doc/search index-name mapping-type :query (q/match-all)
                                 :sort {"title" "asc"})
        hits         (hits-from response)]
    (is (= 4 (total-hits response)))
    (is (= "Nueva York" (-> hits last :_source :title)))
    (is (= "Apache Lucene" (-> hits first :_source :title)))))

;;
;; Missing indices
;;


(deftest test-search-with-ignore-indices
  (let [index-name   "articles"
        missing-index-name "foo"
        mapping-type "article"]
    (is (= 4 (total-hits (doc/search [index-name, missing-index-name] mapping-type 
                                     :query (q/match-all)
                                     :sort {"title" "asc"}
                                     :ignore_indices "missing"))))
    (is (thrown? Exception 
                 (doc/search [index-name, missing-index-name] mapping-type 
                             :query (q/match-all)
                             :sort {"title" "asc"})))))

