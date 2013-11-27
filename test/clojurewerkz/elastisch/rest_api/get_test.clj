(ns clojurewerkz.elastisch.rest-api.get-test
  (:refer-clojure :exclude [replace])
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojurewerkz.elastisch.rest.response :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :each fx/reset-indexes)

(def ^{:const true} index-name "people")
(def ^{:const true} mapping-type "person")


;;
;; get
;;

(deftest test-get-with-non-existing-document
  (is (nil? (doc/get "pages" "page" "as8d8as882jk2jk9#d77$%88s7"))))

(deftest test-get-with-existing-id-that-needs-url-encoding
  (let [id "http://www.faz.net/artikel/C31325/piratenabwehr-keine-kriegswaffen-fuer-private-dienste-30683040.html"]
    (doc/put "pages" "page" id {:url id})
    (is (doc/get "pages" "page" id))))

;;
;; present?
;;

(deftest test-present-with-non-existing-id
  (is (not (doc/present? index-name mapping-type "1"))))

(deftest test-present-with-existing-id
  (doc/put index-name mapping-type "1" fx/person-jack)
  (is (doc/present? index-name mapping-type "1")))


;;
;; mget
;;

(deftest multi-get-test
  (doc/put index-name mapping-type "1" fx/person-jack)
  (doc/put index-name mapping-type "2" fx/person-mary)
  (doc/put index-name mapping-type "3" fx/person-joe)
  (let [mget-result (doc/multi-get
                     [{:_index index-name :_type mapping-type :_id "1"}
                      {:_index index-name :_type mapping-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name
                                   [{:_type mapping-type :_id "1"}
                                    {:_type mapping-type :_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result)))))
  (let [mget-result (doc/multi-get index-name mapping-type
                                   [{:_id "1"} {:_id "2"}])]
    (is (= fx/person-jack (:_source (first mget-result))))
    (is (= fx/person-mary (:_source (second mget-result))))))
