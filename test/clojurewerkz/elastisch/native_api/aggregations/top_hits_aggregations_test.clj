(ns clojurewerkz.elastisch.native-api.aggregations.top-hits-aggregations-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.aggregation   :as a]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.native.response :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-articles-index)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true :aggregation true} test-top-hits-aggregation
    (let [index-name   "articles"
          mapping-type "article"
          response     (doc/search conn index-name mapping-type
                                   {:query (q/match-all)
                                    :aggregations
                                    {:top-hits
                                     {:top_hits {:sort [{:title {:order "asc"}}]
                                                 :size 4}}}})
          agg          (aggregation-from response :top-hits)]
      (is (= 4 (get-in agg [:hits :total])))
      (is (= 4 (count (get-in agg [:hits :hits])))))))
