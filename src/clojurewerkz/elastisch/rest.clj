;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :refer [join]])
  (:import java.net.URLEncoder))

(defrecord Connection
    [uri username password])

(def ^{:dynamic true} *endpoint* (Connection. (or (System/getenv "ELASTICSEARCH_URL")
                                                  "http://localhost:9200")
                                              nil nil))
(def ^:const throw-exceptions false)

(def ^{:const true} slash    "/")
(def ^{:const true} encoding "UTF-8")


(defn post-string
  [^String uri &{:keys [body] :as options}]
  (io! (json/decode (:body (http/post uri (merge options {:accept :json :body body}))) true)))

(defn post
  [^String uri &{:keys [body] :as options}]
  (io! (json/decode (:body (http/post uri (merge options {:accept :json :body (json/encode body)}))) true)))

(defn put
  [^String uri &{:keys [body] :as options}]
  (io! (json/decode (:body (http/put uri (merge options {:accept :json :body (json/encode body) :throw-exceptions throw-exceptions}))) true)))

(defn get
  ([^String uri]
     (io! (json/decode (:body (http/get uri {:accept :json :throw-exceptions throw-exceptions})) true)))
  ([^String uri &{:as options}]
     (io! (json/decode (:body (http/get uri (merge options {:accept :json :throw-exceptions throw-exceptions}))) true))))

(defn head
  [^String uri]
  (io! (http/head uri {:accept :json :throw-exceptions throw-exceptions})))

(defn delete
  ([^String uri]
     (io! (json/decode (:body (http/delete uri {:accept :json :throw-exceptions throw-exceptions})) true)))
  ([^String uri &{:keys [body] :as options}]
     (io! (json/decode (:body (http/delete uri (merge options {:accept :json :body (json/encode body) :throw-exceptions throw-exceptions}))) true))))


(defn url-with-path
  [^Connection conn & segments]
  (str (.uri conn) slash (join slash segments)))

(defn index-url
  [conn index-name]
  (url-with-path conn index-name))

(defn mapping-type-url
  [conn index-name mapping-type]
  (url-with-path conn index-name mapping-type))

(defn search-url
  "Constructs search query URI for the given index (or multiple indexes) and mapping types.

   0-arity form constructs a URI that searches across all indexes and all mappings
   1-arity form constructs a URI for one or more indexes and all mappings in them.
   2-arity form constructs a URI for one or more indexes and given mappings in them.

   Passing index name as \"_all\" means searching across all indexes.

   To specify multiple indexes or mapping types, pass them as collections"
  ([conn]
     (url-with-path conn "_search"))
  ([conn index-name]
     (url-with-path conn index-name "_search"))
  ([conn index-name mapping-type]
     (url-with-path conn index-name mapping-type "_search")))

(defn scroll-url
  ([conn]
     (url-with-path conn "_search" "scroll")))

(defn bulk-url
  ([conn]
     (url-with-path conn "_bulk"))
  ([conn index-name]
     (url-with-path conn index-name "_bulk"))
  ([conn index-name mapping-type]
     (url-with-path conn index-name mapping-type "_bulk")))

(defn multi-search-url
  ([conn]
     (url-with-path conn "_msearch"))
  ([conn index-name]
     (url-with-path conn index-name "_msearch"))
  ([conn index-name mapping-type]
     (url-with-path conn index-name mapping-type "_msearch")))

(defn count-url
  ([conn]
     (url-with-path conn "_count"))
  ([conn index-name mapping-type]
     (url-with-path conn index-name mapping-type "_count")))

(defn record-url
  [conn ^String index-name ^String type id]
  (url-with-path conn index-name type (URLEncoder/encode id encoding)))

(defn record-update-url
  [conn ^String index-name ^String type id]
  (url-with-path conn index-name type (URLEncoder/encode id encoding) "_update"))


(defn index-mapping-url
  ([conn ^String index-name]
     (url-with-path conn index-name "_mapping"))
  ([conn ^String index-name ^String mapping-type]
     (url-with-path conn index-name mapping-type "_mapping")))


(defn index-settings-url
  ([conn]
     (url-with-path conn "_settings"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_settings")))

(defn index-open-url
  [conn ^String index-name]
  (url-with-path conn index-name "_open"))

(defn index-close-url
  [conn ^String index-name]
  (url-with-path conn index-name "_close"))

(defn index-snapshot-url
  [conn ^String index-name]
  (url-with-path conn index-name "_gateway/snapshot"))

(defn index-mget-url
  ([conn]
     (url-with-path conn "_mget"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_mget"))
  ([conn ^String index-name ^String mapping-type]
     (url-with-path conn index-name mapping-type "_mget")))

(defn index-refresh-url
  ([conn]
     (url-with-path conn "_refresh"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_refresh"))
  ([conn ^String index-name ^String mapping-type]
     (url-with-path conn index-name mapping-type "_refresh")))

(defn index-optimize-url
  ([conn]
     (url-with-path conn "_optimize"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_optimize")))

(defn index-flush-url
  ([conn]
     (url-with-path conn "_flush"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_flush")))

(defn index-clear-cache-url
  ([conn]
     (url-with-path conn "_cache/clear"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_cache/clear")))

(defn index-status-url
  ([conn]
     (url-with-path conn "_status"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_status")))

(defn index-stats-url
  ([conn]
     (url-with-path conn "_stats"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_stats")))

(defn index-segments-url
  ([conn]
     (url-with-path conn "_segments"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_segments")))

(defn index-aliases-batch-url
  [conn]
  (url-with-path conn "_aliases"))

(defn index-aliases-url
  ([conn ^String index-name]
     (url-with-path conn index-name "_aliases")))

(defn index-template-url
  [conn ^String template-name]
  (url-with-path conn "_template" template-name))

(defn delete-by-query-url
  ([conn]
     (url-with-path conn "/_all/_query"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_query"))
  ([conn ^String index-name ^String mapping-type]
     (url-with-path conn index-name mapping-type "_query")))

(defn more-like-this-url
  [conn ^String index-name ^String mapping-type id]
  (url-with-path conn index-name mapping-type (URLEncoder/encode id encoding) "_mlt"))

(defn percolator-url
  [conn ^String index-name ^String percolator]
  (url-with-path conn  index-name ".percolator" percolator))

(defn index-percolation-url
  [conn ^String index-name ^String percolator]
  (url-with-path conn index-name percolator "_percolate"))

(defn existing-doc-index-percolation-url
  [conn ^String index-name ^String percolator ^String document-id]
  (url-with-path conn index-name percolator document-id "_percolate"))

(defn query-validation-url
  [conn ^String index-name]
  (url-with-path conn index-name "_validate" "query"))

(defn analyze-url
  ([conn]
     (url-with-path conn "_analyze"))
  ([conn ^String index-name]
     (url-with-path conn index-name "_analyze")))

(defn cluster-health-url
  ([conn ^String index-name]
     (url-with-path conn "_cluster/health" index-name)))

(defn cluster-state-url
  ([conn] (url-with-path conn "_cluster/state")))

(defn cluster-nodes-stats-url
  ([conn ^String nodes ^String attrs]
     (url-with-path conn "_nodes" nodes "stats")))

(defn cluster-nodes-info-url
  ([conn ^String nodes ^String attrs]
     (url-with-path conn "_nodes" nodes attrs)))

(defn snapshot-repository-registration-url
  [conn ^String name]
  (url-with-path conn "_snapshot" name))

(defn snapshot-url
  [conn ^String repo ^String name]
  (url-with-path conn "_snapshot" repo name))

(defn restore-snapshot-url
  [conn ^String repo ^String name]
  (url-with-path conn "_snapshot" repo name "_restore"))


;;
;; API
;;

(defn connect
  "Connects to the given ElasticSearch endpoint and returns it"
  [uri]
  (let [response (get uri)]
    (Connection. uri nil nil)))

(defn connect!
  "Alters default ElasticSearch connection endpoint"
  [uri]
  (alter-var-root (var *endpoint*) (constantly (connect uri))))

