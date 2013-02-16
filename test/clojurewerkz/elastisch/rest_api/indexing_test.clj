(ns clojurewerkz.elastisch.rest-api.indexing-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.rest               :as esr]
            [clojurewerkz.elastisch.query         :as q]

            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test
        [clojurewerkz.elastisch.rest.response :only [ok? acknowledged? conflict? hits-from any-hits? no-hits?]]
        [clj-time.core :only [months ago]]
        [clojure.string :only [join]]))

(use-fixtures :each fx/reset-indexes)


(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")


;;
;; document/put
;;

(deftest ^{:indexing true} test-put-with-autocreated-index
  (let [id         "1"
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (is (idx/exists? index-name))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest ^{:indexing true} test-put-with-precreated-index-without-mapping-types
  (let [id       "1"
        _        (idx/create index-name)
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest ^{:indexing true} test-put-with-precreated-index-with-mapping-types
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        document   fx/person-jack
        response   (doc/put index-name index-type id document)
        get-result (doc/get index-name index-type id)]
    (is (ok? response))
    (are [expected actual] (= expected (actual get-result))
         document   :_source
         index-name :_index
         index-type :_type
         id         :_id
         true       :exists)))

(deftest ^{:indexing true} test-put-with-missing-document-versioning-type
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1)]
    (is (conflict? response))))

(deftest ^{:indexing true} test-put-with-conflicting-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        _        (doc/put index-name index-type id fx/person-mary)
        response (doc/put index-name index-type id fx/person-joe :version 1 :version_type "external")]
    (is (conflict? response))
    (is (not (ok? response)))))

(deftest ^{:indexing true} test-put-with-new-document-version
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack :version 1 :version_type "external")
        _        (doc/put index-name index-type id fx/person-mary :version 2 :version_type "external")
        response (doc/put index-name index-type id fx/person-joe  :version 3 :version_type "external")]
    (is (not (conflict? response)))
    (is (ok? response))))

(deftest ^{:indexing true} create-when-already-created-test
  (let [id       "1"
        _        (doc/put index-name index-type id fx/person-jack)
        response (doc/put index-name index-type id fx/person-joe :op_type "create")]
    (is (conflict? response))))

(deftest ^{:indexing true} test-put-with-a-timestamp
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timestamp (-> 2 months ago))]
    (is (ok? response))))

(deftest ^{:indexing true} test-put-with-a-1-day-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl "1d")]
    (is (ok? response))))

(deftest ^{:indexing true} test-put-with-a-10-seconds-ttl
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :ttl 10000)]
    (is (ok? response))))

(deftest ^{:indexing true} test-put-with-a-timeout
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :timeout "1m")]
    (is (ok? response))))

(deftest ^{:indexing true} test-put-with-refresh-set-to-true
  (let [id       "1"
        _        (idx/create index-name :mappings fx/people-mapping)
        response (doc/put index-name index-type id fx/person-jack :refresh true)]
    (is (ok? response))))


;;
;; document/create
;;

(deftest ^{:indexing true} put-create-autogenerate-id-test
  (let [response (doc/create index-name index-type fx/person-jack)]
    (is (ok? response))
    (is (:_id response))
    (are [expected actual] (= expected (actual response))
         index-name :_index
         index-type :_type
         1          :_version)))


;;
;; custom analyzers
;;

(deftest ^{:indexing true} test-a-custom-analyzer-and-stop-word-list
  (is (ok? (idx/create "alt-tweets"
                       :settings {:index {:analysis {:analyzer {:antiposers {:type      "standard"
                                                                             :filter    ["standard" "lowercase" "stop"]
                                                                             :stopwords ["lol" "rockstar" "ninja" "cloud" "event"]}}}}}
                       :mappings {:tweet {:properties {:text {:type "string" :analyzer "antiposers"}}}})))
  (is (ok? (doc/create "alt-tweets" "tweet" {:text "I am a ninja rockstar brogrammer, yo. I like that event-driven thing."})))
  (idx/refresh "alt-tweets")
  (let [r1 (doc/search "alt-tweets" "tweet" :query (q/query-string :query "text:thing" :default_field :text))
        r2 (doc/search "alt-tweets" "tweet" :query (q/query-string :query "text:(rockstar OR ninja)" :default_field :text))]
    (is (any-hits? r1))
    (is (no-hits? r2))))
