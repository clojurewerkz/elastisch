(ns clojurewerkz.elastisch.rest-api.facets-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(deftest ^{:facets true} test-term-facet-on-tags
  (let [index-name   "articles"
        mapping-type "article"
        ;; match-all here makes faceting act effectively as with :global true but that's fine for this test
        result       (doc/search index-name mapping-type :query (q/match-all) :facets {:tags {:terms {:field :tags}}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))


(deftest ^{:facets true} test-term-facet-on-tags-with-global-scope
  (let [index-name   "articles"
        mapping-type "article"
        result       (doc/search index-name mapping-type :query (q/query-string :query "T*") :facets {:tags {:terms {:field :tags} :global true}})
        facets       (facets-from result)]
    (is (= 0 (-> facets :tags :missing)))
    (is (> (-> facets :tags :total) 25))
    ;; each term is a map with 2 keys: :term and :count
    (is (-> facets :tags :terms first :term))
    (is (-> facets :tags :terms first :count))))
