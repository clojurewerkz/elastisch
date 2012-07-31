(ns clojurewerkz.elastisch.rest
  (:refer-clojure :exclude [get])
  (:require [clj-http.client   :as http]
            [clojure.data.json :as json])
  (:use [clojure.string :only [join]]))

(defrecord ElasticSearchEndpoint
    [uri version])

(def ^{:dynamic true} *endpoint* (ElasticSearchEndpoint. "http://localhost:9200" ""))
(def ^:const throw-exceptions false)

(def ^{:const true} slash "/")


(defn post
  [^String uri &{ :keys [body] :as options }]
  (io! (json/read-json
        (:body (http/post uri (merge options { :accept :json :body (json/json-str body) }))))))

(defn put
  [^String uri &{ :keys [body] :as options}]
  (io! (json/read-json
        (:body (http/put uri (merge options { :accept :json :body (json/json-str body)  :throw-exceptions throw-exceptions }))))))

(defn get
  ([^String uri]
     (io! (json/read-json
           (:body (http/get uri { :accept :json :throw-exceptions throw-exceptions })))))
  ([^String uri &{ :as options }]
     (io! (json/read-json
           (:body (http/get uri (merge options {:accept :json :throw-exceptions throw-exceptions})))))))

(defn head
  [^String uri]
  (io! (http/head uri { :accept :json :throw-exceptions throw-exceptions })))

(defn delete
  ([^String uri]
     (io! (json/read-json
           (:body (http/delete uri { :accept :json :throw-exceptions throw-exceptions })))))
  ([^String uri &{ :keys [body] :as options }]
     (io! (json/read-json
           (:body (http/delete uri (merge options { :accept :json :body (json/json-str body) :throw-exceptions throw-exceptions })))))))


(defn base
  []
  (:uri *endpoint*))

(defn index-url
  [index-name]
  (str (:uri *endpoint*) slash index-name))

(defn mapping-type-url
  [index-name mapping-type]
  (join slash [(:uri *endpoint*) index-name mapping-type]))

(defn search-url
  "Constructs search query URI for the given index (or multiple indexes) and mapping types.

   0-arity form constructs a URI that searches across all indexes and all mappings
   1-arity form constructs a URI for one or more indexes and all mappings in them.
   2-arity form constructs a URI for one or more indexes and given mappings in them.

   Passing index name as \"_all\" means searching across all indexes.

   To specify multiple indexes or mapping types, pass them as collections"
  ([]
     (join slash [(:uri *endpoint*) "_search"]))
  ([index-name]
     (join slash [(:uri *endpoint*) index-name "_search"]))
  ([index-name mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_search"])))

(defn count-url
  ([]
     (str (:uri *endpoint*) slash "_count"))
  ([index-name mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_count"])))

(defn record-url
  [^String index-name ^String type id]
  (join slash [(:uri *endpoint*) index-name type id]))


(defn index-mapping-url
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_mapping"]))
  ([^String index-name ^String mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_mapping"])))


(defn index-settings-url
  ([]
     (str (:uri *endpoint*) slash "_settings"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_settings"])))

(defn index-open-url
  [^String index-name]
  (join slash [(:uri *endpoint*) index-name "_open"]))

(defn index-close-url
  [^String index-name]
  (join slash [(:uri *endpoint*) index-name "_close"]))

(defn index-mget-url
  ([]
     (str (:uri *endpoint*) slash "_mget"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_mget"]))
  ([^String index-name ^String mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_mget"])))

(defn index-refresh-url
  ([]
     (str (:uri *endpoint*) slash "_refresh"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_refresh"]))
  ([^String index-name ^String mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_refresh"])))

(defn index-optimize-url
  ([]
     (str (:uri *endpoint*) slash "_optimize"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_optimize"])))

(defn index-flush-url
  ([]
     (str (:uri *endpoint*) slash "_flush"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_flush"])))


(defn delete-by-query-url
  ([]
     (str (:uri *endpoint*) "/_all/_query"))
  ([^String index-name]
     (join slash [(:uri *endpoint*) index-name "_query"]))
  ([^String index-name ^String mapping-type]
     (join slash [(:uri *endpoint*) index-name mapping-type "_query"])))

(defn more-like-this-url
  [^String index-name ^String mapping-type id]
  (join slash [(:uri *endpoint*) index-name mapping-type id "_mlt"]))


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
