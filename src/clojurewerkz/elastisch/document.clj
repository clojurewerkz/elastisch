(ns clojurewerkz.elastisch.document
  (:refer-clojure :exclude [get replace])
  (:require [clojurewerkz.elastisch.utils         :as utils]
            [clojurewerkz.elastisch.rest          :as rest])
  (:use     clojure.set
            [clojurewerkz.elastisch.response :only [not-found?]]))

(defn put
  [index type id document & {:as all}]
  (rest/put
   (rest/record index type id all)
   :body document))

(defn create
  "Creates document, autogenerating ID"
  [index type document & {:as all}]
  (rest/post
   (rest/index-type index type all)
   :body document))

(defn get
  "Gets Document by Id or returns nil if document is not found."
  [index type id & {:as all}]
  (let [result (rest/get
                (rest/record index type id all))]
    (if (not-found? result)
      nil
      result)))

(defn delete
  [index type id & {:as all}]
  (rest/delete
   (rest/record index type id all)))

(defn present?
  [index type id]
  (not (nil? (get index type id))))

(defn multi-get
  "Multi get returns only items that are present in database."
  ([query]
     (let [results (rest/post
                    (rest/index-mget)
                    :body { :docs query })]
       (filter #(:exists %) (:docs results))))
  ([index query]
     (let [results (rest/post
                    (rest/index-mget index)
                    :body { :docs query })]
       (filter #(:exists %) (:docs results))))
  ([index type query]
     (let [results (rest/post
                    (rest/index-mget index type)
                    :body { :docs query })]
       (filter #(:exists %) (:docs results)))))

(defn search
  "Performs a search query"
  [index-name-or-names type-name-or-names & { :as options }]
  (let [query-string-attributes (select-keys options [:search-type :scroll :size])
        body-attributes         (difference options query-string-attributes)]
    (rest/post
     (rest/search (utils/join-names index-name-or-names) (utils/join-names type-name-or-names) query-string-attributes)
     :body body-attributes)))

(defn replace
  "Replaces document with given id with a new one"
  [idx-name idx-type id document]
  (delete idx-name idx-type id)
  (put idx-name idx-type id document))

;; defn count
;; defn delete-by-query
;; defn more-like-this
