;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest-api.queries.filtered-query-test
  (:require [clojurewerkz.elastisch.rest.document :as doc]
            [clojurewerkz.elastisch.rest :as rest]
            [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clj-time.core :refer [months ago now from-now]]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)

(let [conn (rest/connect)]
  (deftest ^{:rest true :query true} test-basic-filtered-query
    (let [index-name   "people"
          mapping-type "person"
          response     (doc/search conn index-name mapping-type {:query (q/filtered {:query  (q/term :planet "earth")
                                                                                     :filter {:range {:age {:from 20 :to 30}}}})})]
      (is (any-hits? response))
      (is (= 3 (total-hits response))))))
