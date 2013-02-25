(ns clojurewerkz.elastisch.rest-api.facets-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index fx/prepopulate-people-index)

(deftest ^{:facets true :rest true} test-term-facet-on-tags
  (let [index-name   "articles"
        mapping-type "article"
        ;; match-all here makes faceting act effectively as with :global true but that's fine for this test
        result       (doc/search index-name mapping-type :query (q/match-all) :facets {:tags {:terms {:field "tags"}}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))


(deftest ^{:facets true :rest true} test-term-facet-on-tags-with-global-scope
  (let [index-name   "articles"
        mapping-type "article"
        result       (doc/search index-name mapping-type :query (q/query-string :query "T*") :facets {:tags {:terms {:field "tags"} :global true}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))

(deftest ^{:facets true :rest true} test-range-facet-over-age
  (let [index-name   "people"
        mapping-type "person"
        result       (doc/search index-name mapping-type
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
