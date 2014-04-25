;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.delete-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native          :as es]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer [count-from found?]]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (th/connect-native-client)]
  (deftest ^{:native true} test-delete-when-a-document-exists
  (let [id           "1"
        index-name   "people"
        mapping-type "person"]
    (doc/put conn index-name mapping-type id fx/person-jack)
    (is (doc/present? conn index-name mapping-type id))
    (is (found? (doc/delete conn index-name mapping-type id)))
    (is (not (doc/present? conn index-name mapping-type id)))))

(deftest ^{:native true} test-delete-by-query-with-a-term-query-and-mapping
  (let [index-name   "people"
        mapping-type "person"]
    (idx/create conn index-name :mappings fx/people-mapping)
    (doc/create conn index-name mapping-type fx/person-jack)
    (doc/create conn index-name mapping-type fx/person-joe)
    (idx/refresh conn index-name)
    (doc/delete-by-query conn index-name mapping-type (q/term :username "esjoe"))
    (idx/refresh conn index-name)
    (are [c r] (is (= c (count-from r)))
         1 (doc/count conn index-name mapping-type (q/term :username "esjack"))
         0 (doc/count conn index-name mapping-type (q/term :username "esjoe"))
         0 (doc/count conn index-name mapping-type (q/term :username "esmary")))))

(deftest ^{:native true} test-delete-by-query-with-a-term-query-across-all-mappings
  (let [index-name   "people"
        mapping-type "person"]
    (idx/create conn index-name :mappings fx/people-mapping)
    (doc/create conn index-name mapping-type fx/person-jack)
    (doc/create conn index-name mapping-type fx/person-joe)
    (doc/create conn index-name "lawyer" fx/person-joe)
    (idx/refresh conn index-name)
    (doc/delete-by-query-across-all-types conn index-name (q/match-all))
    (idx/refresh conn index-name)
    (are [c r] (is (= c (count-from r)))
         0 (doc/count conn index-name mapping-type (q/match-all)))
    (is (= 0 (count-from (doc/count conn index-name "lawyer" (q/match-all))))))))
