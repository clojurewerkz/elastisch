;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.facets-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native          :as es]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-people-index fx/prepopulate-tweets-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:facets true :native true} test-term-facet-on-tags
  (let [index-name   "articles"
        mapping-type "article"
        ;; match-all here makes faceting act effectively as with :global true but that's fine for this test
        result       (doc/search conn index-name mapping-type
                                 :query (q/match-all)
                                 :facets {:tags {:terms {:field "tags"}}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))

(deftest ^{:facets true :native true} test-term-facet-on-tags-with-global-scope
  (let [index-name   "articles"
        mapping-type "article"
        result       (doc/search conn index-name mapping-type
                                 :query (q/query-string :query "T*")
                                 :facets {:tags {:terms {:field "tags"} :global true}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))

(deftest ^{:facets true :native true} test-range-facet-over-age-1
  (let [index-name   "people"
        mapping-type "person"
        result       (doc/search conn index-name mapping-type
                                 :query (q/match-all)
                                 :facets {:ages {:range {:field "age"
                                                         :ranges [{:from 18 :to 20}
                                                                  {:from 21 :to 25}
                                                                  {:from 26 :to 30}
                                                                  {:from 30 :to 35}
                                                                  {:to 45}]}}})
        facets       (facets-from result)]
    (is (>= 1 (-> facets :ages :ranges second :count)))
    (is (>= 4 (-> facets :ages :ranges last :count)))))

(deftest ^{:facets true :native true} test-range-facet-over-age-2
  (let [index-name   "people"
        mapping-type "person"
        result       (doc/search conn index-name mapping-type
                                 :query (q/match-all)
                                 :facets {:ages {:histogram {:field    "age"
                                                             :interval 5}}})
        facets       (facets-from result)]
    (is (>= 1 (-> facets :ages :entries first :count)))
    (is (>= 2 (-> facets :ages :entries second :count)))
    (is (>= 1 (-> facets :ages :entries last :count)))))

(deftest ^{:facets true :native true} test-date-histogram-facet-on-post-dates
  (let [index-name   "tweets"
        mapping-type "tweet"
        result       (doc/search conn index-name mapping-type
                                 :query (q/match-all)
                                 :facets {:dates {:date_histogram {:field   "timestamp"
                                                                   :interval "day"}}})
        facets       (facets-from result)]
    (is (= [1343606400000 1343692800000 1343779200000 1343865600000]
           (vec (map :time (get-in facets [:dates :entries]))))))))

