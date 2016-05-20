(ns clojurewerkz.elastisch.native-api.suggest-test
  (:require [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.test.helpers :as th]
            [clojure.test :refer :all]))

(let [conn (th/connect-native-client)]
  (use-fixtures :each fx/reset-indexes
                      fx/prepopulate-people-suggestion
                      fx/prepopulate-people-category-suggestion
                      fx/prepopulate-people-location-suggestion)

  (deftest ^{:native true} test-suggest-complete-people-names
    (testing "simple autocomplete returns mary's username"
      (let [index-name "people_suggestions"
            res (doc/suggest conn index-name :completion "esma" {})]
        (is (map? (:hits res)))
        (let [payload (-> res :hits :options first :payload)]
          (is (= "esma" (-> res :hits :text)))
          (is (= "esmary" (:username payload)))
          (is (= "Mary" (:first-name payload)))
          (is (= "Lindey" (:last-name payload)))))))

  (deftest ^{:native true} test-fuzzy-suggest-complete-people-names
    (testing "simple fuzzy autocomplete returns mary's username even i had typo"
      (let [index-name "people_suggestions"
            res (doc/suggest conn index-name :fuzzy "esmor" {:fuzziness 1 :min-length 2})]
        (is (map? (:hits res)))
        (let [payload (-> res :hits :options first :payload)]
          (is (= "esmor" (-> res :hits :text)))
          (is (= "esmary" (:username payload)))
          (is (= "Mary" (:first-name payload)))
          (is (= "Lindey" (:last-name payload)))))))

  (deftest ^{:native true} test-suggest-complete-with-category-context
    (testing "autocomplete filters results by person's gender"
      (let [index-name "people_with_category"
            res (doc/suggest conn index-name :completion "e" {:context {:gender "female"}})]
        (is (map? (:hits res)))
        (is (= 2 (-> res :hits :options count)))
        (is (= "esmary" (-> res :hits :options first :text))))))

  (deftest ^{:native true} test-suggest-complete-with-location-context
    (testing "autocomplete returns only matches nearby"
      (let [index-name "people_with_locations"
            opts {:context {:location {:lat 90.0 :lon 90.1}}}
            res (doc/suggest conn index-name :completion "es" opts)]
        (is (map? (:hits res)))
        (is (= 1 (-> res :hits :options count)))
        (is (= "esjack" (-> res :hits :options first :text))))))

  (deftest ^{:native true} test-fuzzy-suggest-complete-with-category-context
    (testing "autocomplete filters results by person's gender"
      (let [index-name "people_with_category"
            res (doc/suggest conn index-name :fuzzy "es" {:context {:gender "female"}})]
        (is (map? (:hits res)))
        (is (= 2 (-> res :hits :options count)))
        (is (= "esmary" (-> res :hits :options first :text))))))

  (deftest ^{:native true} test-fuzzy-suggest-complete-with-location-context
    (testing "autocomplete returns only matches nearby"
      (let [index-name "people_with_locations"
            opts {:context {:location {:lat 90.23 :lon 90.56}}}
            res (doc/suggest conn index-name :fuzzy "es" opts)]
        (is (map? (:hits res)))
        (is (= 1 (-> res :hits :options count)))
        (is (= "esjack" (-> res :hits :options first :text)))))))
