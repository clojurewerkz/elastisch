(ns clojurewerkz.elastisch.rest-api.multi-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest          :as rest]
            [clojurewerkz.elastisch.rest.multi    :as multi]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures      :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-articles-index fx/prepopulate-tweets-index)

(deftest test-multi-search
  "Verify that multi/search returns same result as singularly executed queries."
  (let [res1 (doc/search "people" "person" :query (q/match-all) :size 1)
        res2 (doc/search "articles" "article" :query (q/match-all) :size 1)
        multires (multi/search [{:index "people" :type "person"} {:query (q/match-all) :size 1}
                                {:index "articles" :type "article"} {:query (q/match-all) :size 1}])]
    (is (= (-> res1 :hits :hits first :_source)
           (-> multires first :hits :hits first :_source)))
    (is (= (-> res2 :hits :hits first :_source)
           (-> multires second :hits :hits first :_source)))))

(deftest test-multi-with-index-and-type
  (let [res1 (doc/search "people" "person" :query (q/term :planet "earth"))
        res2 (doc/search "people" "person" :query (q/term :first-name "mary"))
        multires (multi/search-with-index-and-type "people" "person"
                                                   [{} {:query (q/term :planet "earth")}
                                                    {} {:query (q/term :first-name "mary")}])]
    (is (= (-> res1 :hits) (-> multires first :hits)))
    (is (= (-> res2 :hits) (-> multires second :hits)))))