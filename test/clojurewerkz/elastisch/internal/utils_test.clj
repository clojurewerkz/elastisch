(ns clojurewerkz.elastisch.internal.utils-test
  (:require [clojurewerkz.elastisch.rest.utils :as utils]
            [clojure.test :refer :all]))

(deftest join-names-test
  (is (= "name" (utils/join-names "name")))
  (is (= "name1,name2" (utils/join-names ["name1", "name2"]))))
