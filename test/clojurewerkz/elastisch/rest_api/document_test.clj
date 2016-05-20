(ns clojurewerkz.elastisch.rest-api.document-test
  (:require [clojurewerkz.elastisch.rest :as es]
            [clojurewerkz.elastisch.rest.document :as es.document]
            clj-http.core
            [clojure.test :refer :all])
  (:import java.io.ByteArrayInputStream
           clojure.lang.ExceptionInfo))

(deftest ^:rest test-http-options-overridability
  (let [default-conn (es/connect)
        throwing-conn (es/connect "http://localhost:9200"
                                  {:throw-exceptions true})
        no-throwing-conn (es/connect "http://localhost:9200"
                                     {:throw-exceptions false})
        index "arbitrary_index"
        mapping "arbitrary_mapping"
        id "arbitrary_id"
        doc {"arbitrary_attribute" "arbitrary value"}]
    (with-redefs [clj-http.core/request (constantly {:status 400
                                                     :body (.getBytes "{\"error\": \"This is an example exception.\", \"status\": 400}"
                                                                      "UTF-8")})]
      (testing "that the functions that default to swallowing exceptions can be configured to actually throw them"
        (is (= (es.document/put default-conn index mapping id doc)
               {:error "This is an example exception.", :status 400}))
        (is (thrown-with-msg? ExceptionInfo
                              #"clj-http: status 400"
                              (es.document/put throwing-conn index mapping id doc))))
      (testing "that the functions that default to throwing exceptions can be configured to swallow them"
        (is (thrown-with-msg? ExceptionInfo
                              #"clj-http: status 400"
                              (es.document/create default-conn index mapping doc {:id id})))
        (is (= (es.document/create no-throwing-conn index mapping doc {:id id})
               {:error "This is an example exception.", :status 400}))))))
