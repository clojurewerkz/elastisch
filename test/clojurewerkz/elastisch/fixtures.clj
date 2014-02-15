(ns clojurewerkz.elastisch.fixtures
  (:require [clojurewerkz.elastisch.rest.index    :as idx]
            [clojurewerkz.elastisch.rest.document :as doc]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.rest.response :refer [created?]]))

(defn reset-indexes*
  []
  ;; deletes all indices
  (idx/delete)
  (idx/delete-template "accounts"))

(defn reset-indexes
  [f]
  (reset-indexes*)
  (f)
  (reset-indexes*))

;;
;; People
;;

(def person-jack
  {:username   "esjack"
   :first-name "Jack"
   :last-name  "Black"
   :title      "Sales Manager"
   :biography  "Tries to avoid eating fat, being good to other people and does sports every now and then"
   :planet     "Earth"
   :age 22})

(def person-joe
  {:username   "esjoe"
   :first-name "Joe"
   :last-name  "Mahjo"
   :title      "Trader"
   :biography  "Quite a nice guy"
   :planet     "Earth"
   :age 37})

(def person-mary
  {:username   "esmary"
   :first-name "Mary"
   :last-name  "Lindey"
   :title      "Copywriter"
   :biography  "Writes copy and copies writes"
   :planet     "Earth"
   :age 28})

(def person-tony
  {:username   "estony"
   :first-name "Tony"
   :last-name  "Hall"
   :title      "Yak Shaver"
   :biography  "yak/reduce all day long"
   :planet     "Earth"
   :age 29})

(def people-mapping
  {:person {:properties {:username   {:type "string" :store "yes"}
                         :first-name {:type "string" :store "yes"}
                         :last-name  {:type "string"}
                         :age        {:type "integer"}
                         :title      {:type "string" :analyzer "snowball"}
                         :planet     {:type "string"}
                         :biography  {:type "string" :analyzer "snowball" :term_vector "with_positions_offsets"}}}})



;;
;; Articles
;;

(def article-on-elasticsearch
  {:url "http://en.wikipedia.org/wiki/ElasticSearch"
   :title "ElasticSearch"
   :language "English"
   :tags "technology, opensource, search, full-text search, distributed, software, lucene"
   :summary "ElasticSearch is a distributed, RESTful, free/open source search server based on Apache Lucene.
             It is developed by Shay Banon and is released under the Apache Software License. ElasticSearch can be used to search all kind of documents.
             It provides a scalable search solution, has near real-time search and support for multitenancy."
   :number-of-edits 10
   :latest-edit {:date "2012-03-26T06:07:00"
                 :author nil}})

(def article-on-lucene
  {:url "http://en.wikipedia.org/wiki/Apache_Lucene"
   :title "Apache Lucene"
   :language "English"
   :tags "technology, opensource, search, full-text search, distributed, software, lucene"
   :summary "Apache Lucene is a free/open source information retrieval software library, originally created in Java by Doug Cutting. It is
             supported by the Apache Software Foundation and is released under the Apache Software License."
   :number-of-edits 48
   :latest-edit {:date "2012-03-11T02:19:00"
                 :author "Thorwald"}})

(def article-on-nueva-york
  {:url "http://es.wikipedia.org/wiki/Nueva_York"
   :title "Nueva York"
   :language "Spanish"
   :tags "geografía, EEUU, historia, ciudades, Norteamérica"
   :summary "Nueva York (en inglés y oficialmente, New York City) es la ciudad más poblada del Estado de Nueva York, de los
             Estados Unidos de América y la segunda aglomeración urbana del continente. Es el centro del área metropolitana de Nueva York,
             la cual está entre las aglomeraciones urbanas más grandes del mundo."
   :number-of-edits 73887})

(def article-on-austin
  {:url "http://es.wikipedia.org/wiki/Austin"
   :title "Austin"
   :language "Spanish"
   :tags "geografía, EEUU, historia, ciudades, Norteamérica"
   :summary "Austin es una ciudad y capital estatal, ubicada en los condados de Travis, Williamson y Hays,
             en el estado estadounidense de Texas."
   :number-of-edits 13002})

(def articles-mapping
  {:article {:properties {:title    {:type "string" :analyzer "snowball"}
                          :summary  {:type "string" :analyzer "snowball"}
                          :url      {:type "string"}
                          :language {:type "string"}
                          :tags     {:type "string" :analyzer "standard"}
                          :number-of-edits {:type "long"}
                          :latest-edit {:type       "object"
                                        :properties {:date   {:type "date" :fuzzy_factor 3}
                                                     :author {:type "string" :index "not_analyzed" :null_value "N/A"}}}}}})

;;
;; Tweets
;;

(def tweets-mapping {:tweet {:properties {:username  {:type "string" :index "not_analyzed"}
                                          :text      {:type "string" :analyzer "standard"}
                                          :timestamp {:type "date" :include_in_all false :format "basic_date_time_no_millis"}
                                          :retweets  {:type "integer" :include_in_all false}
                                          :promoted  {:type "boolean" :default false :boost 10.0 :include_in_all false}
                                          :location  {:type "object" :include_in_all false :properties {:country {:type "string" :index "not_analyzed"}
                                                                                                        :state   {:type "string" :index "not_analyzed"}
                                                                                                        :city    {:type "string" :index "not_analyzed"}}}}}})

(def tweet1
  {:username  "clojurewerkz"
   :text      "Elastisch beta3 is out, several more @elasticsearch features supported github.com/clojurewerkz/elastisch, improved docs http://clojureelasticsearch.info #clojure"
   :timestamp "20120802T101232+0100"
   :retweets  1
   :location  {:country "Russian Federation"
               :state   "Moscow"
               :city    "Moscow"}})


(def tweet2
  {:username  "ifesdjeen"
   :text      "Did I mention that Glitch Mob is amazing?"
   :timestamp "20120801T174722+0100"
   :retweets  0
   :location  {:country "Germany"
               :state   "Bavaria"
               :city    "Munich"}})

(def tweet3
  {:username  "michaelklishin"
   :text      "I am late to the party but congrats to both @old_sound and VMware on getting him on the team"
   :timestamp "20120731T223900+0300"
   :retweets  2
   :location  {:country "Russian Federation"
               :state   "Moscow"
               :city    "Moscow"}})

(def tweet4
  {:username  "michaelklishin"
   :text      "Why Kafka performs so well (tl;dr: its authors know how shit really works) http://www.quora.com/Apache-Kafka/Kafka-writes-every-message-to-broker-disk-Still-performance-wise-it-is-better-than-some-of-the-in-memory-message-storing-message-queues-Why-is-that/answer/Jay-Kreps"
   :timestamp "20120731T011200+0300"
   :retweets  3
   :location  {:country "Russian Federation"
               :state   "Moscow"
               :city    "Moscow"}})

(def tweet5
  {:username  "DEVOPS_BORAT"
   :text      "OpenStack is Esperanto of cloud. Same adoption."
   :timestamp "20120731T232300-0800"
   :retweets  0
   :location  {:country "USA"
               :state   "CA"
               :city    "San Francisco"}})


(defn prepopulate-people-index
  [f]
  (let [index-name   "people"
        mapping-type "person"]
    (idx/create index-name :mappings people-mapping)

    (is (created? (doc/put index-name mapping-type "1" person-jack)))
    (is (created? (doc/put index-name mapping-type "2" person-mary)))
    (is (created? (doc/put index-name mapping-type "3" person-joe)))
    (is (created? (doc/put index-name mapping-type "4" person-tony)))

    (idx/refresh index-name)
    (f)))

(defn prepopulate-articles-index
  [f]
  (let [index-name   "articles"
        mapping-type "article"]
    (idx/create index-name :mappings articles-mapping)

    (is (created? (doc/put index-name mapping-type "1" article-on-elasticsearch)))
    (is (created? (doc/put index-name mapping-type "2" article-on-lucene)))
    (is (created? (doc/put index-name mapping-type "3" article-on-nueva-york)))
    (is (created? (doc/put index-name mapping-type "4" article-on-austin)))
    (idx/refresh index-name)
    (f)))


(defn prepopulate-tweets-index
  [f]
  (let [index-name   "tweets"
        mapping-type "tweet"]
    (idx/create index-name :mappings tweets-mapping)

    (is (created? (doc/put index-name mapping-type "1" tweet1)))
    (is (created? (doc/put index-name mapping-type "2" tweet2)))
    (is (created? (doc/put index-name mapping-type "3" tweet3)))
    (is (created? (doc/put index-name mapping-type "4" tweet4)))
    (is (created? (doc/put index-name mapping-type "5" tweet5)))

    (idx/refresh index-name)
    (f)))
