(ns clojurewerkz.elastisch.document
  (:refer-clojure :exclude [get replace count])
  (:require [clojurewerkz.elastisch.rest          :as rest])
  (:use     clojure.set
            [clojurewerkz.elastisch.utils :only [join-names]]
            [clojurewerkz.elastisch.response :only [not-found?]]))

;;
;; API
;;

(defn create
  "Adds document to the search index. Document id will be generated automatically"
  [index mapping-type document & {:as params}]
  (rest/post (rest/index-type-url index mapping-type) :body document :query-params params))

(defn put
  "Adds document to the search index, using given document id"
  ([index mapping-type id document]
     (rest/put (rest/record-url index mapping-type id) :body document))
  ([index mapping-type id document & {:as params}]
     (rest/put (rest/record-url index mapping-type id) :body document :query-params params)))

(defn get
  "Gets Document by Id or returns nil if document is not found."
  [index mapping-type id & {:as params}]
  (let [result (rest/get (rest/record-url index mapping-type id) :query-params params)]
    (if (not-found? result)
      nil
      result)))

(defn delete
  "Deletes document from the index"
  ([index mapping-type id]
     (rest/delete (rest/record-url index mapping-type id)))
  ([index mapping-type id & {:as params}]
     (rest/delete (rest/record-url index mapping-type id) :query-params params)))

(defn present?
  [index mapping-type id]
  (not (nil? (get index mapping-type id))))

(defn multi-get
  "Multi get returns only items that are present in database."
  ([query]
     (let [results (rest/post (rest/index-mget-url)
                              :body { :docs query })]
       (filter :exists (:docs results))))
  ([index query]
     (let [results (rest/post (rest/index-mget-url index)
                              :body { :docs query })]
       (filter :exists (:docs results))))
  ([index mapping-type query]
     (let [results (rest/post (rest/index-mget-url index mapping-type)
                              :body { :docs query })]
       (filter :exists (:docs results)))))

(defn search
  "Performs a search query"
  [index mapping-type & { :as options }]
  (let [qk   [:search_type :scroll :size]
        qp   (select-keys options qk)
        body (dissoc options qk)]
    (rest/post (rest/search-url (join-names index) (join-names mapping-type))
               :body body
               :query-params qp)))

(defn replace
  "Replaces document with given id with a new one"
  [index mapping-type id document]
  (delete index mapping-type id)
  (put index mapping-type id document))

(defn count
  "Performs a count query.

   For Elastic Search reference, see http://www.elasticsearch.org/guide/reference/api/count.html"
  ([index mapping-type]
     (rest/post (rest/count-url) (join-names index) (join-names mapping-type)))
  ([index mapping-type query]
     (rest/post (rest/count-url) (join-names index) (join-names mapping-type) :body query))
  ([index mapping-type query & { :as options }]
     (let [qk   [:q :df :analyzer :default_operator]
           qp   (select-keys options qk)]
       (rest/post (rest/count-url (join-names index) (join-names mapping-type))
                  :query-params qp
                  :body query))))

(defn delete-by-query
  "Performs a delete-by-query operation.

   For Elastic Search reference, see http://www.elasticsearch.org/guide/reference/api/delete-by-query.html"
  ([index mapping-type query]
     (rest/delete (rest/delete-by-query-url index mapping-type) (join-names index) (join-names mapping-type) :body query))
  ([index mapping-type query & { :as options }]
     (let [qk   [:q :df :analyzer :default_operator]
           qp   (select-keys options qk)]
       (rest/delete (rest/delete-by-query-url (join-names index) (join-names mapping-type))
                    :query-params qp
                    :body query))))


(defn more-like-this
  [index mapping-type id &{:as params}]
  (rest/get (rest/more-like-this-url index mapping-type id)
            :query-params params))

;; TODO percolate
;; TODO multi-search