(ns elastisch.test.fixtures
  (:require [elastisch.index       :as index]))

(defn delete-people-index
  [f]
  (index/delete "people")
  (f)
  (index/delete "people"))

(def person-jack {
  :first-name "Jack"
  :last-name  "Black"
  :title      "Sales Manager"
  :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then" })

(def person-joe {
  :first-name "Joe"
  :last-name  "Mahjo"
  :title      "Trader"
  :biography  "Quite a nice guy" })

(def person-mary {
  :first-name "Mary"
  :last-name  "Lindey"
  :title      "Copywriter"
  :biography  "Writes copy and copies writes" })

(def people-mapping {
  :person {
    :properties {
      :first-name { :type "string" :store "yes" }
      :last-name  { :type "string" }
      :title      { :type "string" :analyzer "snowball" }
      :biography  { :type "string" :analyzer "snowball"}}}})