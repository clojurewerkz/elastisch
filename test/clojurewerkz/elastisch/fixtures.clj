(ns clojurewerkz.elastisch.fixtures
  (:require [clojurewerkz.elastisch.index :as index]))

(defn reset-indexes*
  []
  (doseq [i ["people"]]
    (index/delete i)))

(defn reset-indexes
  [f]
  (reset-indexes*)
  (f)
  (reset-indexes*))

(def person-jack
  {:username   "esjack"
   :first-name "Jack"
   :last-name  "Black"
   :title      "Sales Manager"
   :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then"
   :planet     "Earth"})

(def person-joe
  {:username   "esjoe"
   :first-name "Joe"
   :last-name  "Mahjo"
   :title      "Trader"
   :biography  "Quite a nice guy"
   :planet     "Earth"})

(def person-mary
  {:username   "esmary"
   :first-name "Mary"
   :last-name  "Lindey"
   :title      "Copywriter"
   :biography  "Writes copy and copies writes"
   :planet     "Earth"})

(def people-mapping
  { :person { :properties {:username   { :type "string" :store "yes" }                        
                           :first-name { :type "string" :store "yes" }
                           :last-name  { :type "string" }
                           :title      { :type "string" :analyzer "snowball" }
                           :planet     { :type "string" }
                           :biography  { :type "string" :analyzer "snowball"}}}})
