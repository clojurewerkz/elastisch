(ns clojurewerkz.elastisch.native-api.suggest-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.test.helpers :as th]
            [clojure.test :refer :all]))

;;run: ES_URL="X" ES_CLUSTER_NAME="X" ES_CLUSTER_HOST ... 
;; ES_URL="http://192.168.99.100:9200" ES_CLUSTER_NAME="skillable_search_dev" ES_CLUSTER_HOST="192.168.99.100" lein with-profile dev test clojurewerkz.elastisch.native-api.suggest-test


;;(use-fixtures :each fx/reset-indexes fx/prepopulate-people-suggestion)
(let [conn (th/connect-native-client)]
  (use-fixtures :each fx/reset-indexes fx/prepopulate-people-suggestion)

  (deftest ^{:native true} test-suggest-complete-people-names
    (testing "simple autocomplete returns mary's username"
      (let [index-name "people"
            res (doc/suggest conn index-name :completion "esma" {})]
        (is (= 1 (count (:hits res))))
        (let [match1 (first (:hits res))
              payload (-> match1 :options first :payload)]
          (is (= "esma" (:text match1)))
          (is (= "esmary" (:username payload)))
          (is (= "Mary" (:first-name payload)))
          (is (= "Lindey" (:last-name payload))))))))
