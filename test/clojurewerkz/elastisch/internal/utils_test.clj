;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.internal.utils-test
  (:require [clojurewerkz.elastisch.rest.utils :as utils]
            [clojure.test :refer :all]))

(deftest join-names-test
  (is (= "name" (utils/join-names "name")))
  (is (= "name1,name2" (utils/join-names ["name1", "name2"]))))
