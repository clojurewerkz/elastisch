(ns clojurewerkz.elastisch.indices-test
  (:refer-clojure :exclude [get replace count])
  (:require [clj-http.client         :as http]
            [clojurewerkz.elastisch.rest     :as rest]
            [clojurewerkz.elastisch.index    :as idx]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojurewerkz.elastisch.document
        clojurewerkz.elastisch.response
        clojure.test))

(use-fixtures :each fx/reset-indexes)

;;
;; create, delete, exists?
;;

(deftest test-successful-creation-of-index
  (let [index    "people"
        response (idx/create index :mappings fx/people-mapping)]
    (is (ok? response))
    (is (idx/exists? index))))

(deftest test-successful-deletion-of-index
  (let [index    "people"
        _        (idx/create index :mappings fx/people-mapping)
        response (idx/delete index)]
    (is (ok? response))
    (is (not (idx/exists? index)))))

;;
;; Mappings
;;

(deftest test-getting-index-mapping
  (let [index    "people"
        mappings fx/people-mapping
        response (idx/create index :mappings mappings)]
    (is (ok? response))
    (is (= mappings (:people (idx/get-mapping index))))
    (is (= mappings (:people (idx/get-mapping [index, "shmeople"]))))
    (is (= mappings (idx/get-mapping index "person")))))

(deftest test-updating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings { :person { :properties { :first-name { :type "string" } } } })
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (= (ok? response)))
    (is (= mapping (:people (idx/get-mapping index))))))

(deftest test-updating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings { :person { :properties { :first-name { :type "string" :store "no" } } } })
        response (idx/update-mapping index "person" :mapping mapping :ignore_conflicts false)]
    (is (= (ok? response)))))

(deftest test-creating-index-mapping
  (let [index    "people"
        mapping  fx/people-mapping
        _        (idx/create index :mappings {})
        response (idx/update-mapping index "person" :mapping mapping)]
    (is (ok? response))
    (is (= mapping (:people (idx/get-mapping index))))))

(deftest test-delete-index-mapping
  (let [index       "people"
        index-type  "person"
        _           (idx/create index :mappings fx/people-mapping)
        response    (idx/delete-mapping index index-type)]
    (is (ok? response))
    (is (nil? ((idx/get-mapping index) index-type)))))

;;
;; Settings
;;

(deftest test-geting-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :settings settings :mappings fx/people-mapping)]
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest test-updating-global-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

(deftest testing-updating-specific-index-settings
  (let [index     "people"
        settings  { :index { :refresh_interval "1s" } }
        _         (idx/create index :mappings fx/people-mapping)
        response  (idx/update-settings index settings)]
    (is (ok? response))
    (is (= "1s" (get-in (idx/get-settings "people") [:people :settings :index.refresh_interval])))))

;;
;; Open/close
;;

(deftest open-close-index-test
  (let [index     "people"
        _         (idx/create index :mappings fx/people-mapping)]
    (is (ok? (idx/open index)))
    (is (ok? (idx/close index)))))
