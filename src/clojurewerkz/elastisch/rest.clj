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
  (str (base) slash index-name))

(defn index-type-url
  [index-name index-type]
  (join slash [(base) index-name index-type]))

(defn search-url
  [index-name index-type]
  (join slash [(base) index-name index-type "_search"]))

(defn count-url
  ([]
     (str (base) slash "_count"))
  ([index-name index-type]
     (join slash [(base) index-name index-type "_count"])))

(defn record-url
  [^String index-name ^String type id]
  (join slash [(base) index-name type id]))


(defn index-mapping-url
  ([^String index-name]
     (join slash [(base) index-name "_mapping"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_mapping"])))


(defn index-settings-url
  ([]
     (str (base) slash "_settings"))
  ([^String index-name]
     (join slash [(base) index-name "_settings"])))

(defn index-open-url
  [^String index-name]
  (join slash [(base) index-name "_open"]))

(defn index-close-url
  [^String index-name]
  (join slash [(base) index-name "_close"]))

(defn index-mget-url
  ([]
     (str (base) slash "_mget"))
  ([^String index-name]
     (join slash [(base) index-name "_mget"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_mget"])))

(defn index-refresh-url
  ([]
     (str (base) slash "_refresh"))
  ([^String index-name]
     (join slash [(base) index-name "_refresh"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_refresh"])))


(defn delete-by-query-url
  ([^String index-name]
     (join slash [(base) index-name "_query"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_query"])))

(defn more-like-this-url
  [^String index-name ^String index-type id]
  (join slash [(base) index-name index-type id "_mlt"]))


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
