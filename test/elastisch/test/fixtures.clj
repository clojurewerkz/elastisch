(ns elastisch.test.fixtures
  (:require [elastisch.index       :as index]))

(defn delete-people-index
  [f]
  (index/delete "people")
  (f)
  (index/delete "people"))

(def jack {
  :first-name "Jack"
  :last-name  "Black"
  :title      "Sales Manager"
  :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then" })

(def people-mapping {
                     :person {
                              :_source { :enabled false }
                              :properties {
                                           :first-name { :type "string" }
                                           :last-name  { :type "string" }
                                           :title      { :type "string" :analyzer "snowball" }
                                           :biography  { :type "string" :analyzer "snowball"}
                                           }
                              }
                     })
