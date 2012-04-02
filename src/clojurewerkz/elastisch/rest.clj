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
  "Returns index mapping"
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


(defn connect
  [uri]
  (let [response (get uri)]
    (ElasticSearchEndpoint. uri
                            (:number (:version response)))))

(defn connect!
  [uri]
  (alter-var-root (var *endpoint*) (constantly (connect uri))))
