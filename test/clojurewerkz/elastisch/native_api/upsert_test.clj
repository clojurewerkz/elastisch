;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.upsert-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query           :as q]
            [clojurewerkz.elastisch.fixtures        :as fx]
            [clojurewerkz.elastisch.test.helpers    :as th]
            [clojurewerkz.elastisch.native.response :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(let [conn (th/connect-native-client)]
  (deftest test-upserting-documents
    (let [index-name "people"
          index-type "person"
          id         "3"
          new-bio    "Such a brilliant person"]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/put conn index-name index-type "1" fx/person-jack)
      (doc/put conn index-name index-type "2" fx/person-mary)
      (doc/put conn index-name index-type "3" fx/person-joe)

      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type {:query (q/term :biography "nice")})))
      (is (no-hits?  (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})))
      (doc/upsert conn index-name index-type id (assoc fx/person-joe :biography new-bio))
      (idx/refresh conn index-name)
      (is (any-hits? (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})))
      (is (no-hits?  (doc/search conn index-name index-type {:query (q/term :biography "nice")})))))

  (deftest test-upserting-with-separate-upsert-document
    (let [index-name "people"
          index-type "person"
          new-bio    "Such a brilliant person"
          new-joe    (assoc fx/person-joe :biography new-bio)]
      (idx/create conn index-name {:mappings fx/people-mapping})

      (doc/put conn index-name index-type "1" fx/person-joe)
      (doc/upsert conn index-name index-type "1" fx/person-joe {:upsert new-joe})
      (doc/upsert conn index-name index-type "2" fx/person-joe {:upsert new-joe})

      (idx/refresh conn index-name)
      (let [result (doc/search conn index-name index-type {:query (q/term :biography "nice")})]
        (is (= 1 (total-hits result)))
        (is (= "1" (:_id (first (get-in result [:hits :hits]))))))
      (let [result (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})]
        (is (= 1 (total-hits result)))
        (is (= "2" (:_id (first (get-in result [:hits :hits]))))))))

  (deftest test-upserting-document-with-parent
    (let [index-name "people"
          index-type "person"
          child-index-name "passports"
          child-index-type "passport"]
      (idx/create conn index-name  {:mappings fx/people-mapping})
      (idx/create conn child-index-name {:mappings fx/passport-mapping})
      (doc/put conn index-name index-type "1" fx/person-jack)
      (doc/upsert conn child-index-name child-index-type "123000456000" {"id" "123000456000"} {:parent "1"})))

  (deftest test-upserting-document-with-retry-on-conflict
    (let [index-name "people"
          index-type "person"
          id         "3"
          new-bio    "Such a brilliant person"]
      (idx/create conn index-name {:mappings fx/people-mapping})
      (doc/put conn index-name index-type "1" fx/person-jack)
      (is (no-hits?  (doc/search conn index-name index-type {:query (q/term :biography "nice")})))
      (is (no-hits?  (doc/search conn index-name index-type {:query (q/term :biography "brilliant")})))
      (doall
        (pmap
          (fn [i]
            (doc/upsert conn index-name index-type id (assoc fx/person-joe :biography new-bio) {:retry-on-conflict 10}))
          (repeat 5 5)))
      (idx/refresh conn index-name)
      (is (any-hits?  (doc/search conn index-name index-type {:query (q/term :biography "brilliant")}))))))
