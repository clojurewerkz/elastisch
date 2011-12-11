(ns elastish.test.core
  (:require [clj-http.client      :as http]
            [elastish.rest-client :as rest]
            [elastish.index       :as index]
            [elastish.urls        :as urls]

            )
  (:use [elastish.core]
        [clojure.test]))

(defn ok?
  [response]
  (:ok response))

(deftest replace-me
  (println
   (ok?
    (rest/post-req
     (urls/index-record "test" "test-type" 1)
     :body { :user "kimchy" :post_date "2009-11-15T14:12:12" :message "trying out Elastic Search" })
    ))

  (index/delete "test2")

  (let [mappings { :test-type {
                               :_source  { :enabled false }
                               :properties { :field1 { :type "string" :index "not_analyzed" }}} }]
    (index/create "test2" :settings {} :mappings mappings))


  (println
   (rest/get-req
    (urls/index-mapping "test2" "test-type")))
  )