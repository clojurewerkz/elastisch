;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.facets-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-people-index fx/prepopulate-tweets-index)

(let [conn (rest/connect)]
  (deftest  ^{:facets true :rest true} test-term-facet-on-tags
    (let [index-name   "articles"
          mapping-type "article"
          ;; match-all here makes faceting act effectively as with :global true but that's fine for this test
          result       (doc/search conn index-name mapping-type {:query (q/match-all) :facets {:tags {:terms {:field "tags"}}}})
          facets       (facets-from result)]
      (is (= 0 (-> facets :tags :missing)))
      (is (> (-> facets :tags :total) 25))
      ;; each term is a map with 2 keys: :term and :count
      (is (-> facets :tags :terms first :term))
      (is (-> facets :tags :terms first :count))))


  (deftest ^{:facets true :rest true} test-term-facet-on-tags-with-global-scope
    (let [index-name   "articles"
          mapping-type "article"
          result       (doc/search conn index-name mapping-type {:query (q/query-string :query "T*") :facets {:tags {:terms {:field "tags"} :global true}}})
          facets       (facets-from result)]
      (is (= 0 (-> facets :tags :missing)))
      (is (> (-> facets :tags :total) 25))
      ;; each term is a map with 2 keys: :term and :count
      (is (-> facets :tags :terms first :term))
      (is (-> facets :tags :terms first :count))))

  (deftest ^{:facets true :rest true} test-range-facet-over-age-1
    (let [index-name   "people"
          mapping-type "person"
          result       (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :facets {:ages {:range {:field "age"
                                                            :ranges [{:from 18 :to 20}
                                                                     {:from 21 :to 25}
                                                                     {:from 26 :to 30}
                                                                     {:from 30 :to 35}
                                                                     {:to 45}]}}}})
          facets       (facets-from result)]
      (is (>= (-> facets :ages :ranges second :count) 1))
      (is (>= (-> facets :ages :ranges last :count) 4))))

  (deftest ^{:facets true :rest true} test-range-facet-over-age-2
    (let [index-name   "people"
          mapping-type "person"
          result       (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :facets {:ages {:histogram {:field    "age"
                                                                :interval 5}}}})
          facets       (facets-from result)]
      (is (>= (-> facets :ages :entries first :count) 1))
      (is (>= (-> facets :ages :entries second :count) 2))
      (is (>= (-> facets :ages :entries last :count) 1))))

  (deftest ^{:facets true :rest true} test-date-histogram-facet-on-post-dates
    (let [index-name   "tweets"
          mapping-type "tweet"
          result       (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :facets {:dates {:date_histogram {:field   "timestamp"
                                                                      :interval "day"}}}})
          facets       (facets-from result)]
      (is (= [{:time 1343606400000 :count 1}
              {:time 1343692800000 :count 1}
              {:time 1343779200000 :count 2}
              {:time 1343865600000 :count 1}]
             (-> facets :dates :entries))))))
