(ns clojurewerkz.elastisch.fixtures
  (:require [clojurewerkz.elastisch.index :as index]))

(defn delete-people-index
  [f]
  (index/delete "people")
  (f)
  (index/delete "people"))

(def person-jack
  {:username   "esjack"
   :first-name "Jack"
   :last-name  "Black"
   :title      "Sales Manager"
   :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then" })

(def person-joe
  {:username   "esjoe"
   :first-name "Joe"
   :last-name  "Mahjo"
   :title      "Trader"
   :biography  "Quite a nice guy" })

(def person-mary
  {:username   "esmary"
   :first-name "Mary"
   :last-name  "Lindey"
   :title      "Copywriter"
   :biography  "Writes copy and copies writes" })

(def people-mapping
  { :person { :properties {:username   { :type "string" :store "yes" }                        
                           :first-name { :type "string" :store "yes" }
                           :last-name  { :type "string" }
                           :title      { :type "string" :analyzer "snowball" }
                           :biography  { :type "string" :analyzer "snowball"}}}})
