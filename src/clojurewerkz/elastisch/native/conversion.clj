(ns clojurewerkz.elastisch.native.conversion
  (:refer-clojure :exclude [get merge flush])
  (:require [clojure.walk :as wlk])
  (:import [org.elasticsearch.common.settings Settings ImmutableSettings ImmutableSettings$Builder]
           [org.elasticsearch.common.transport
            TransportAddress InetSocketTransportAddress LocalTransportAddress]
           java.util.Map
           clojure.lang.IPersistentMap
           [org.elasticsearch.common.xcontent XContentType]
           [org.elasticsearch.index VersionType]
           [org.elasticsearch.action.index IndexRequest IndexResponse]
           [org.elasticsearch.action.get GetRequest GetResponse]
           [org.elasticsearch.action.admin.indices.exists.indices IndicesExistsRequest]
           [org.elasticsearch.action.admin.indices.create CreateIndexRequest]
           [org.elasticsearch.action.admin.indices.delete DeleteIndexRequest]
           [org.elasticsearch.action.admin.indices.stats IndicesStatsRequest]
           [org.elasticsearch.action.admin.indices.settings UpdateSettingsRequest]
           [org.elasticsearch.action.admin.indices.open OpenIndexRequest]
           [org.elasticsearch.action.admin.indices.close CloseIndexRequest]))

;;
;; Implementation
;;

(def ^{:const true}
  default-content-type XContentType/JSON)

(defprotocol XContentTypeConversion
  (^XContentType to-content-type [input] "Picks a content type for given input"))

(extend-protocol XContentTypeConversion
  clojure.lang.Named
  (to-content-type [input]
    (to-content-type (name input)))

  String
  (to-content-type [^String s]
    (case (.toLowerCase s)
      "application/json" XContentType/JSON
      "text/json"        XContentType/JSON
      "json"             XContentType/JSON

      "application/smile" XContentType/SMILE
      "smile"             XContentType/SMILE
      XContentType/JSON))

  XContentType
  (to-content-type [input]
    input))

(defprotocol VersionTypeConversion
  (^VersionType to-version-type [input] "Picks a content type for given input"))

(extend-protocol VersionTypeConversion
  clojure.lang.Named
  (to-version-type [input]
    (to-version-type (name input)))

  String
  (to-version-type [^String s]
    (case (.toLowerCase s)
      "internal" VersionType/INTERNAL
      "external" VersionType/EXTERNAL
      VersionType/INTERNAL))

  VersionType
  (to-version-type [input]
    input))


;;
;; API
;;

;;
;; Settings
;;

(defn ^Settings ->settings
  "Converts a Clojure map into immutable ElasticSearch settings"
  [m]
  (if m
    (let [^ImmutableSettings$Builder sb (ImmutableSettings/settingsBuilder)]
      (doseq [[k v] m]
        (.put sb ^String (name k) ^String v))
      (.build sb))
    ImmutableSettings$Builder/EMPTY_SETTINGS))

;;
;; Transports
;;

(defn ^TransportAddress ->socket-transport-address
  [^String host ^long port]
  (InetSocketTransportAddress. host port))

(defn ^TransportAddress ->local-transport-address
  [^String id]
  (LocalTransportAddress. id))


;;
;; Indexing
;;

(defn ^IndexRequest ->index-request
  "Builds an index action request"
  ([index mapping-type ^Map doc]
     ;; default content type used by IndexRequest is JSON. MK.
     (-> (IndexRequest. (name index) (name mapping-type))
         (.source ^Map (wlk/stringify-keys doc))))
  ;; non-variadic because it is more convenient and efficient to
  ;; invoke this internal implementation fn this way. MK.
  ([index mapping-type ^Map doc ^String {:keys [id
                                                routing
                                                parent
                                                timestamp
                                                ttl
                                                op-type
                                                refresh
                                                version-type
                                                percolate
                                                content-type]}]
     (let [ir (-> (IndexRequest. (name index) (name mapping-type))
                  (.source ^Map (wlk/stringify-keys doc)))]
       (when id
         (.id ir id))
       (when content-type
         (.contentType ir (to-content-type content-type)))
       (when routing
         (.routing ir routing))
       (when parent
         (.parent ir parent))
       (when timestamp
         (.timestamp ir timestamp))
       (when ttl
         (.ttl ir ttl))
       (when op-type
         (.opType ir ^String (.toLowerCase (name op-type))))
       (when refresh
         (.refresh ir))
       (when version-type
         (.versionType ir (to-version-type version-type)))
       (when percolate
         (.percolate ir percolate))
       ir)))

(defn ^IPersistentMap index-response->map
  "Converts an index action response to a Clojure map"
  [^IndexResponse r]
  ;; underscored aliases are there to match REST API responses
  {:index    (.index r)
   :_index   (.index r)
   :id       (.id r)
   :_id      (.id r)
   :type     (.type r)
   :_type    (.type r)
   :version  (.version r)
   :_version (.version r)
   :matches  (.matches r)})


;;
;; Get requests
;;

(defn ^GetRequest ->get-request
  "Builds a get action request"
  ([index mapping-type ^String id]
     (GetRequest. (name index) (name mapping-type) id))
  ([index mapping-type ^String id {:keys [parent preference
                                          routing fields]}]
     (let [gr (GetRequest. (name index) (name mapping-type) id)]
       (when routing
         (.routing gr routing))
       (when parent
         (.parent gr parent))
       (when preference
         (.preference gr preference))
       (when fields
         (.fields gr (into-array String fields)))
       gr)))

(defn ^IPersistentMap get-response->map
  [^GetResponse r]
  (let [s (wlk/keywordize-keys (into {} (.sourceAsMap r)))]
    ;; underscored aliases are there to match REST API responses
    {:exists? (.exists r)
     :exists  (.exists r)
     :index   (.index r)
     :_index  (.index r)
     :type    (.type r)
     :_type   (.type r)
     :id      (.id r)
     :_id     (.id r)
     :version  (.version r)
     :_version (.version r)
     :empty?   (.isSourceEmpty r)
     :source   s
     :_source  s
     ;; TODO: convert GetFields to maps
     :fields   (into {} (.fields r))}))


;;
;; Admin operations
;;

(defn ^IndicesExistsRequest ->index-exists-request
  [index-name]
  (let [ary (if (coll? index-name)
              (into-array String index-name)
              (into-array String [index-name]))]
    (IndicesExistsRequest. ary)))

(defn ^CreateIndexRequest ->create-index-request
  [index-name settings mappings]
  (let [r (CreateIndexRequest. index-name)
        m (wlk/stringify-keys mappings)]
    (when settings
      (.settings r ^Map settings))
    (when mappings
      (doseq [[k v] m]
        (.mapping r ^String k ^Map v)))
    r))

(defn ^DeleteIndexRequest ->delete-index-request
  [index-name]
  (if (coll? index-name)
    (DeleteIndexRequest. (into-array String [index-name]))
    (DeleteIndexRequest. ^String index-name)))

(defn ^UpdateSettingsRequest ->update-settings-request
  [index-name settings]
  (let [ary (if (coll? index-name)
              (into-array String index-name)
              (into-array String [index-name]))
        m   (wlk/stringify-keys settings)]
    (doto (UpdateSettingsRequest. ary)
      (.settings ^Map m))))

(defn ^OpenIndexRequest ->open-index-request
  [index-name]
  (OpenIndexRequest. index-name))

(defn ^CloseIndexRequest ->close-index-request
  [index-name]
  (CloseIndexRequest. index-name))

(defn ^IndicesStatsRequest ->index-stats-request
  ([]
     (let [r (IndicesStatsRequest.)]
       (.all r)
       r))
  ([{:keys [docs store indexing types groups get
            search merge flush refresh]}]
     (let [r   (IndicesStatsRequest.)]
       (when docs
         (.docs r docs))
       (when store
         (.store r store))
       (when indexing
         (.indexing r indexing))
       (when types
         (.types r (into-array String types)))
       (when groups
         (.groups r (into-array String groups)))
       (when get
         (.get r get))
       (when search
         (.search r search))
       (when merge
         (.merge r merge))
       (when flush
         (.flush r flush))
       (when refresh
         (.refresh r refresh))
       r)))
