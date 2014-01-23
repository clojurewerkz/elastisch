(ns clojurewerkz.elastisch.rest
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.string :refer [join]])
  (:import java.net.URLEncoder))

(defrecord ElasticSearchEndpoint
    [uri version])

(def ^{:dynamic true} *endpoint* (ElasticSearchEndpoint. (or (System/getenv "ELASTICSEARCH_URL")
                                                             "http://localhost:9200") ""))
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
  [& segments]
  (str (:uri *endpoint*) slash (join slash segments)))

(defn index-url
  [index-name]
  (url-with-path index-name))

(defn mapping-type-url
  [index-name mapping-type]
  (url-with-path index-name mapping-type))

(defn search-url
  "Constructs search query URI for the given index (or multiple indexes) and mapping types.

   0-arity form constructs a URI that searches across all indexes and all mappings
   1-arity form constructs a URI for one or more indexes and all mappings in them.
   2-arity form constructs a URI for one or more indexes and given mappings in them.

   Passing index name as \"_all\" means searching across all indexes.

   To specify multiple indexes or mapping types, pass them as collections"
  ([]
     (url-with-path "_search"))
  ([index-name]
     (url-with-path index-name "_search"))
  ([index-name mapping-type]
     (url-with-path index-name mapping-type "_search")))

(defn scroll-url
  ([]
     (url-with-path "_search" "scroll")))

(defn bulk-url
  ([]
     (url-with-path "_bulk"))
  ([index-name]
     (url-with-path index-name "_bulk"))
  ([index-name mapping-type]
     (url-with-path index-name mapping-type "_bulk")))

(defn multi-search-url
  ([]
     (url-with-path "_msearch"))
  ([index-name]
     (url-with-path index-name "_msearch"))
  ([index-name mapping-type]
     (url-with-path index-name mapping-type "_msearch")))

(defn count-url
  ([]
     (url-with-path "_count"))
  ([index-name mapping-type]
     (url-with-path index-name mapping-type "_count")))

(defn record-url
  [^String index-name ^String type id]
  (url-with-path index-name type (URLEncoder/encode id encoding)))

(defn record-update-url
  [^String index-name ^String type id]
  (url-with-path index-name type (URLEncoder/encode id encoding) "_update"))


(defn index-mapping-url
  ([^String index-name]
     (url-with-path index-name "_mapping"))
  ([^String index-name ^String mapping-type]
     (url-with-path index-name mapping-type "_mapping")))


(defn index-settings-url
  ([]
     (url-with-path "_settings"))
  ([^String index-name]
     (url-with-path index-name "_settings")))

(defn index-open-url
  [^String index-name]
  (url-with-path index-name "_open"))

(defn index-close-url
  [^String index-name]
  (url-with-path index-name "_close"))

(defn index-snapshot-url
  [^String index-name]
  (url-with-path index-name "_gateway/snapshot"))

(defn index-mget-url
  ([]
     (url-with-path "_mget"))
  ([^String index-name]
     (url-with-path index-name "_mget"))
  ([^String index-name ^String mapping-type]
     (url-with-path index-name mapping-type "_mget")))

(defn index-refresh-url
  ([]
     (url-with-path "_refresh"))
  ([^String index-name]
     (url-with-path index-name "_refresh"))
  ([^String index-name ^String mapping-type]
     (url-with-path index-name mapping-type "_refresh")))

(defn index-optimize-url
  ([]
     (url-with-path "_optimize"))
  ([^String index-name]
     (url-with-path index-name "_optimize")))

(defn index-flush-url
  ([]
     (url-with-path "_flush"))
  ([^String index-name]
     (url-with-path index-name "_flush")))

(defn index-clear-cache-url
  ([]
     (url-with-path "_cache/clear"))
  ([^String index-name]
     (url-with-path index-name "_cache/clear")))

(defn index-status-url
  ([]
     (url-with-path "_status"))
  ([^String index-name]
     (url-with-path index-name "_status")))

(defn index-stats-url
  ([]
     (url-with-path "_stats"))
  ([^String index-name]
     (url-with-path index-name "_stats")))

(defn index-segments-url
  ([]
     (url-with-path "_segments"))
  ([^String index-name]
     (url-with-path index-name "_segments")))

(defn index-aliases-batch-url
  []
  (url-with-path "_aliases"))

(defn index-aliases-url
  ([^String index-name]
     (url-with-path index-name "_aliases")))

(defn index-template-url
  [^String template-name]
  (url-with-path "_template" template-name))

(defn delete-by-query-url
  ([]
     (url-with-path "/_all/_query"))
  ([^String index-name]
     (url-with-path index-name "_query"))
  ([^String index-name ^String mapping-type]
     (url-with-path index-name mapping-type "_query")))

(defn more-like-this-url
  [^String index-name ^String mapping-type id]
  (url-with-path index-name mapping-type (URLEncoder/encode id encoding) "_mlt"))

(defn percolator-url
  [^String index-name ^String percolator]
  (url-with-path "_percolator" index-name percolator))

(defn index-percolation-url
  [^String index-name ^String percolator]
  (url-with-path index-name percolator "_percolate"))

(defn query-validation-url
  [^String index-name]
  (url-with-path index-name "_validate" "query"))

(defn analyze-url 
  ([] (url-with-path "_analyze"))
  ([^String index-name] (url-with-path index-name "_analyze")))

(defn cluster-health-url 
  ([& index-names] (url-with-path "_cluster/health" (apply str (interpose "," index-names)))))

(defn cluster-state-url 
  ([] (url-with-path "_cluster/state")))

;;
;; API
;;

(defn connect
  "Connects to the given ElasticSearch endpoint and returns it"
  [uri]
  (let [response (get uri)]
    (ElasticSearchEndpoint. uri (get-in response [:version :number]))))

(defn connect!
  "Alters default ElasticSearch connection endpoint"
  [uri]
  (alter-var-root (var *endpoint*) (constantly (connect uri))))

