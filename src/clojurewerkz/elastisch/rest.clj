(ns clojurewerkz.elastisch.rest
  (:refer-clojure :exclude [get])
  (:require [clojurewerkz.elastisch.utils :as utils]
            [clj-http.client   :as http]
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
           (:body (http/get uri (merge {:accept :json :throw-exceptions throw-exceptions} options)))))))

(defn head
  [^String uri]
  (io! (http/head uri { :accept :json :throw-exceptions throw-exceptions })))

(defn delete
  [^String uri]
  (io! (json/read-json
        (:body (http/delete uri { :accept :json :throw-exceptions throw-exceptions })))))


(defn base
  []
  (:uri *endpoint*))

(defn index
  [^String index-name]
  (join slash [(base) index-name]))

(defn index-type
  [^String index-name ^String index-type]
  (join slash [(base) index-name index-type]))

(defn search
  [^String index-name ^String index-type]
  (join slash [(base) index-name index-type "_search"]))

(defn record
  [^String index-name ^String type id]
  (join slash [(base) index-name type id]))


(defn index-mapping
  "Returns index mapping"
  ([^String index-name]
     (join slash [(base) index-name "_mapping"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_mapping"])))


(defn index-settings
  ([]
     (str (base) slash "_settings"))
  ([^String index-name]
     (join slash [(base) index-name "_settings"])))

(defn index-open
  [^String index-name]
  (join slash [(base) index-name "_open"]))

(defn index-close
  [^String index-name]
  (join slash [(base) index-name "_close"]))

(defn index-mget
  ([]
     (str (base) slash "_mget"))
  ([^String index-name]
     (join slash [(base) index-name "_mget"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_mget"])))

(defn index-refresh
  ([]
     (str (base) slash "_refresh"))
  ([^String index-name]
     (join slash [(base) index-name "_refresh"]))
  ([^String index-name ^String index-type]
     (join slash [(base) index-name index-type "_refresh"])))


(defn connect
  [uri]
  (let [response (get uri)]
    (ElasticSearchEndpoint. uri
                            (:number (:version response)))))

(defn connect!
  [uri]
  (alter-var-root (var *endpoint*) (constantly (connect uri))))
