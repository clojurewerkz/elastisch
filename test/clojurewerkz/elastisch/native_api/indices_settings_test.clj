;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.indices-settings-test
  (:require [clojurewerkz.elastisch.native        :as es]
            [clojurewerkz.elastisch.native
              [document :as doc]
              [index :as idx]
              [response :refer [acknowledged?]]]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index)

(let [conn (th/connect-native-client)
      index-name "people"]
  (deftest ^{:indexing true :native true} test-closing-and-opening-existing-index
    (testing "it should close people's index"
      (let [res (idx/close conn index-name)]
        (is (acknowledged? res))))

    (testing "it should open people's index"
      (let [res (idx/open conn index-name)]
        (is (acknowledged? res))))))
