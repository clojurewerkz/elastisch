(ns clojurewerkz.elastisch.native-api.indexing-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.native          :as es]
            [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.native.index    :as idx]
            [clojurewerkz.elastisch.query    :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.test.helpers :as th])
  (:use clojure.test
        [clojurewerkz.elastisch.native.response :only [hits-from any-hits? no-hits?]]
        [clojure.string :only [join]]))

(def ^{:const true} index-name "people")
(def ^{:const true} index-type "person")

(when (not (th/ci?))
  (es/connect! [["127.0.0.1" 9300]]
               {"cluster.name" (System/getenv "ES_CLUSTER_NAME") })

  (use-fixtures :each fx/reset-indexes)


  ;;
  ;; document/put
  ;;

  (deftest ^{:indexing true :native true} test-sync-put-with-autocreated-index
    (let [id         "1"
          document   fx/person-jack
          response   (doc/put index-name index-type id document)
          get-result (doc/get index-name index-type id)]
      (is (idx/exists? index-name))
      (are [expected actual] (= expected (actual get-result))
           document   :_source
           index-name :_index
           index-type :_type
           id         :_id
           true       :exists))))
