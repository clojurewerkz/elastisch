;; Copyright 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.native.conversion
  (:refer-clojure :exclude [get merge flush])
  (:require [clojure.walk :as wlk]
            [cheshire.core :as json])
  (:import [org.elasticsearch.common.settings Settings Settings$Builder]
           [org.elasticsearch.common.transport
            TransportAddress InetSocketTransportAddress LocalTransportAddress]
           java.util.Map
           clojure.lang.IPersistentMap
           java.net.InetAddress
           org.elasticsearch.client.Client
           org.elasticsearch.common.xcontent.XContentType
           org.elasticsearch.index.VersionType
           [org.elasticsearch.search.highlight HighlightBuilder HighlightBuilder$Field
            HighlightField]
           org.elasticsearch.common.text.Text
           ;; Actions
           org.elasticsearch.action.ShardOperationFailedException
           [org.elasticsearch.action.index IndexRequest IndexRequest$OpType IndexResponse]
           [org.elasticsearch.index.get GetResult]
           [org.elasticsearch.action.get GetRequest GetResponse MultiGetRequest MultiGetResponse MultiGetItemResponse]
           [org.elasticsearch.action.delete DeleteRequest DeleteResponse]
           [org.elasticsearch.action.update UpdateRequest UpdateResponse]
           [org.elasticsearch.action.count CountRequest CountResponse]
           [org.elasticsearch.action.search SearchRequest SearchResponse SearchScrollRequest
            MultiSearchAction MultiSearchRequestBuilder MultiSearchRequest MultiSearchResponse MultiSearchResponse$Item]
           [org.elasticsearch.action.suggest SuggestRequest SuggestResponse]
           [org.elasticsearch.search.suggest.completion CompletionSuggestionBuilder
                                                        CompletionSuggestionFuzzyBuilder]
           [org.elasticsearch.common.unit Fuzziness] ;;for CompletionSuggestionFuzzyBuilder
           ;[org.elastisearch.search.suggest.phrase PhraseSuggestionBuilder]
           [org.elasticsearch.search.suggest.term TermSuggestionBuilder]

           [org.elasticsearch.search.builder SearchSourceBuilder]
           [org.elasticsearch.search.sort SortBuilder SortOrder FieldSortBuilder]
           [org.elasticsearch.search SearchHits SearchHit]
           [org.elasticsearch.action.percolate PercolateRequestBuilder PercolateResponse PercolateResponse$Match]
           ;; Aggregations
           org.elasticsearch.search.aggregations.Aggregations
           org.elasticsearch.search.aggregations.metrics.avg.Avg
           org.elasticsearch.search.aggregations.metrics.max.Max
           org.elasticsearch.search.aggregations.metrics.min.Min
           org.elasticsearch.search.aggregations.metrics.sum.Sum
           org.elasticsearch.search.aggregations.metrics.tophits.TopHits
           [org.elasticsearch.search.aggregations.metrics.percentiles Percentiles Percentile]
           org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality
           org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount
           org.elasticsearch.search.aggregations.metrics.stats.Stats
           org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats
           [org.elasticsearch.search.aggregations.bucket.histogram  Histogram Histogram$Bucket]
           [org.elasticsearch.search.aggregations.bucket.range      Range     Range$Bucket]
           [org.elasticsearch.search.aggregations.bucket.terms      Terms     Terms$Bucket]
           org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation
           [org.elasticsearch.search.aggregations HasAggregations]
           ;; Administrative Actions
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           [org.elasticsearch.action.admin.indices.mapping.get GetMappingsRequest GetMappingsResponse]
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest
           org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           [org.elasticsearch.action.admin.indices.forcemerge ForceMergeAction ForceMergeRequest] 
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           [org.elasticsearch.action.admin.indices.segments IndicesSegmentsRequest IndicesSegmentResponse IndexSegments]
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest
           com.carrotsearch.hppc.cursors.ObjectObjectCursor
           org.elasticsearch.common.collect.ImmutableOpenMap
           org.elasticsearch.cluster.metadata.MappingMetaData
           org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest
           org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest
           org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest
           org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest
           org.elasticsearch.action.support.broadcast.BroadcastResponse
           org.elasticsearch.action.support.master.AcknowledgedResponse
           org.elasticsearch.search.fetch.source.FetchSourceContext
           ;; Bulk responses
           [org.elasticsearch.action.bulk BulkResponse BulkItemResponse]))

;;
;; Implementation
;;

(defn ^{:tag "[Ljava.lang.String;"} ->string-array
  "Coerces argument to an array of strings"
  [index-name]
  (if (coll? index-name)
    (into-array String index-name)
    (into-array String [index-name])))

(defprotocol DeepMapConversion
  (deep-java-map->map [o]))

(extend-protocol DeepMapConversion
  java.util.Map
  (deep-java-map->map [o]
    (reduce (fn [m [^String k v]]
              (assoc m (keyword k) (deep-java-map->map v)))
            {}
            (.entrySet o)))

  java.util.List
  (deep-java-map->map [o]
    (vec (map deep-java-map->map o)))

  java.lang.Object
  (deep-java-map->map [o] o)

  nil
  (deep-java-map->map [_] nil))

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
  "Converts a Clojure map into immutable Elasticsearch settings"
  [m]
  (if m
    (let [^Settings$Builder sb (Settings/builder)]
      (doseq [[k v] m]
        (.put sb ^String (name k) v))
      (.build sb))
    Settings$Builder/EMPTY_SETTINGS))

;;
;; Transports
;;

(defn ^TransportAddress ->socket-transport-address
  [^String host ^{:tag "long"} port]
  (InetSocketTransportAddress. (InetAddress/getByName host) port))

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
         (.source ^String (json/encode doc))))
  ;; non-variadic because it is more convenient and efficient to
  ;; invoke this internal implementation fn this way. MK.
  ([index mapping-type ^Map doc {:keys [id
                                        routing
                                        parent
                                        timestamp
                                        ttl
                                        op-type
                                        refresh
                                        version
                                        version-type
                                        content-type]}]
     (let [ir (-> (IndexRequest. (name index) (name mapping-type))
                  (.source ^String (json/encode doc)))]
       (when id
         (.id ^IndexRequest ir ^String id))
       (when content-type
         (.contentType ^IndexRequest ir (to-content-type content-type)))
       (when routing
         (.routing ^IndexRequest ir ^String routing))
       (when parent
         (.parent ^IndexRequest ir ^String parent))
       (when timestamp
         (.timestamp ^IndexRequest ir timestamp))
       (when ttl
         (.ttl ir ttl))
       (when op-type
         (.opType ^IndexRequest ir (IndexRequest$OpType/fromString (.toLowerCase (name op-type)))))
       (when refresh
         (.refresh ^IndexRequest ir refresh))
       (when version
         (.version ^IndexRequest ir version))
       (when version-type
         (.versionType ^IndexRequest ir (to-version-type version-type)))
       ir)))

(defn ^IPersistentMap index-response->map
  "Converts an index action response to a Clojure map"
  [^IndexResponse r]
  ;; underscored aliases are there to match REST API responses
  {:index    (.getIndex r)
   :_index   (.getIndex r)
   :id       (.getId r)
   :_id      (.getId r)
   :type     (.getType r)
   :_type    (.getType r)
   :version  (.getVersion r)
   :_version (.getVersion r)})


;;
;; Actions
;;

(defn ^GetRequest ->get-request
  "Builds a get action request"
  ([index mapping-type ^String id]
     (GetRequest. (name index) (name mapping-type) id))
  ([index mapping-type ^String id {:keys [parent preference
                                          routing fields _source]}]
     (let [gr (GetRequest. (name index) (name mapping-type) id)]
       (when routing
         (.routing gr routing))
       (when parent
         (.parent gr parent))
       (when preference
         (.preference gr preference))
       (when fields
         (.fields gr (into-array String fields)))
       (when _source
         (let [^{:tag "[Ljava.lang.String;"} exclude (when (:exclude _source)
                             (into-array String (:exclude _source)))
               ^{:tag "[Ljava.lang.String;"} include (when (or (not exclude)        ; either include = _source
                                 (:include _source))  ; or it's given explicitly
                         (into-array String (:include _source _source)))]
           (.fetchSourceContext gr (FetchSourceContext. include exclude))))
       gr)))

(defn- convert-source-result
  "Copied from clj-elasticsearch. More performant than doing wlk/keywordize-keys."
  [src]
  (cond
   (instance? java.util.HashMap src) (into {}
                                           (map (fn [^java.util.Map$Entry e]
                                                  [(keyword (.getKey e))
                                                   (convert-source-result (.getValue e))]) src))
   (instance? java.util.ArrayList src) (into [] (map convert-source-result src))
   :else src))

(defn- convert-fields-result
  "Get fields from search result, i.e. when filtering returned fields."
  [fields]
  (cond
   (instance? java.util.Map fields) (into {} (map (fn [^java.util.Map$Entry e]
                                                    [(keyword (.getKey e))
                                                     (.. e getValue getValues)]) fields))
   :else fields))

(defn ^IPersistentMap get-response->map
  [^GetResponse r]
  (let [s (convert-source-result (.getSourceAsMap r))]
    ;; underscored aliases are there to match REST API responses
    {:exists? (.isExists r)
     :exists  (.isExists r)
     :index   (.getIndex r)
     :_index  (.getIndex r)
     :type    (.getType r)
     :_type   (.getType r)
     :id      (.getId r)
     :_id     (.getId r)
     :version  (.getVersion r)
     :_version (.getVersion r)
     :empty?   (.isSourceEmpty r)
     :source   s
     :_source  s
     ;; TODO: convert GetFields to maps
     :fields   (into {} (.getFields r))}))

(defn ^IPersistentMap get-result->map
  [^GetResult r]
  (let [s (convert-source-result (.sourceAsMap r))]
    {:exists? (.isExists r)
     :exists  (.isExists r)
     :index   (.getIndex r)
     :_index  (.getIndex r)
     :type    (.getType r)
     :_type   (.getType r)
     :id      (.getId r)
     :_id     (.getId r)
     :version  (.getVersion r)
     :_version (.getVersion r)
     :empty?   (.isSourceEmpty r)
     :source   s
     :_source  s
     ;; TODO: convert GetFields to maps
     :fields   (into {} (.getFields r))}))

(defn ^IPersistentMap multi-get-item-response->map
  [^MultiGetItemResponse i]
  (let [r  (.getResponse i)
        s  (convert-source-result (.getSourceAsMap r))]
    {:exists   (.isExists r)
     :_index   (.getIndex r)
     :_type    (.getType r)
     :_id      (.getId r)
     :_version (.getVersion r)
     :_source  s}))

(defn multi-get-response->seq
  [^MultiGetResponse r]
  (let [items (.getResponses r)]
    (map multi-get-item-response->map items)))

(defn ^MultiGetRequest ->multi-get-request
  "Builds a multi-get action request"
  ([queries]
     (->multi-get-request queries {}))
  ([queries {:keys [preference refresh realtime]}]
     (let [r (MultiGetRequest.)]
       (doseq [q queries]
         (.add r (:_index q) (:_type q) (:_id q)))
       (when preference
         (.preference r preference))
       (when refresh
         (.refresh r refresh))
       (when realtime
         (.realtime r realtime))
       r)))

(defn ^CountRequest ->count-request
  ([index-name options]
     (->count-request index-name [] options))
  ([index-name mapping-type {:keys [^Map source min-score routing]}]
     (let [^CountRequest r (CountRequest. (->string-array index-name))]
       (.types r (->string-array mapping-type))
       (when source
         (let [^Map m {"query" (wlk/stringify-keys source)}]
           (.source r m)))
       (when min-score
         (.minScore r min-score))
       (when routing
         (.routing r (->string-array routing)))
       r)))

(defn ^DeleteRequest ->delete-request
  ([index-name mapping-type id]
     (DeleteRequest. (name index-name) (name mapping-type) id))
  ([index-name mapping-type id {:keys [routing refresh version version-type parent]}]
     (let [r (DeleteRequest. (name index-name) (name mapping-type) id)]
       (when routing
         (.routing ^DeleteRequest r ^String routing))
       (when refresh
         (.refresh ^DeleteRequest r ^{:tag "boolean"} refresh))
       (when version
         (.version ^DeleteRequest r ^{:tag "long"} version))
       (when version-type
         (.versionType ^DeleteRequest r version-type))
       (when parent
         (.parent ^DeleteRequest r parent))
       r)))

(defn ^IPersistentMap delete-response->map
  [^DeleteResponse r]
  ;; matches REST API responses
  {:found    (.isFound r)
   :found?   (.isFound r)
   :_index   (.getIndex r)
   :_type    (.getType r)
   :_version (.getVersion r)
   :_id      (.getId r)})

(defn ^UpdateRequest ->update-request
  [index-name mapping-type ^String id ^Map doc {:keys [doc_as_upsert
                                                       fields
                                                       parent
                                                       refresh
                                                       retry_on_conflict
                                                       routing
                                                       script
                                                       script_params
                                                       scripted_upsert]}]
     (let [r (UpdateRequest. (name index-name) (name mapping-type) id)
           stringified-doc (wlk/stringify-keys doc)]
       (when (and doc (not script))
         (.doc ^UpdateRequest r ^Map stringified-doc))
       (when doc_as_upsert
         (.docAsUpsert ^UpdateRequest r ^Boolean doc_as_upsert))
       (when fields
         (.fields ^UpdateRequest r (->string-array fields)))
       (when parent
         (.parent ^UpdateRequest r parent))
       (when script
         (.script ^UpdateRequest r ^String script))
       (when scripted_upsert
         (.upsert ^UpdateRequest r ^Map stringified-doc)
         (.scriptedUpsert ^UpdateRequest r ^Boolean scripted_upsert))
       (when script_params
         (.scriptParams ^UpdateRequest r ^Map (wlk/stringify-keys script_params)))
       (when retry_on_conflict
         (.retryOnConflict ^UpdateRequest r ^Boolean retry_on_conflict))
       (when refresh
         (.refresh ^UpdateRequest r ^Boolean refresh))
       (when routing
         (.routing ^UpdateRequest r ^String routing))
       r))

(defn ^UpdateRequest ->partial-update-request
  [index-name mapping-type ^String id ^Map partial-doc {:keys [routing refresh retry-on-conflict fields parent]}]
     (let [doc (wlk/stringify-keys partial-doc)
           r   (UpdateRequest. (name index-name) (name mapping-type) id)]
       (.doc r ^Map doc)
       (when refresh
         (.refresh r refresh))
       (when retry-on-conflict
         (.retryOnConflict r retry-on-conflict))
       (when routing
         (.routing r ^String routing))
       (when parent
         (.parent r parent))
       (when fields
         (.fields r (->string-array fields)))
       r))

(defn ^UpdateRequest ->upsert-request
  ([index-name mapping-type ^String id ^Map doc {:keys [routing refresh retry-on-conflict fields parent]}]
     (let [doc (wlk/stringify-keys doc)
           r   (UpdateRequest. (name index-name) (name mapping-type) id)]
       (.doc r ^Map doc)
       (.upsert r ^Map doc)
       (when refresh
         (.refresh r refresh))
       (when retry-on-conflict
         (.retryOnConflict r retry-on-conflict))
       (when routing
         (.routing r ^String routing))
       (when parent
         (.parent r parent))
       (when fields
         (.fields r (->string-array fields)))
       r)))

(defn ^IPersistentMap update-response->map
  [^UpdateResponse r]
  ;; matches REST API responses
  ;; example: {:ok true, :_index people, :_type person, :_id 1, :_version 2}
  {:_index (.getIndex r) :type (.getType r) :_id (.getId r)
   :get-result (when-let [gr (.getGetResult r)]
                 (get-result->map gr))})

(defn ^SortOrder ^:private ->sort-order
  [s]
  (if (instance? SortOrder s)
    s
    (SortOrder/valueOf (.toUpperCase (name s)))))

(defn ^SortBuilder ^:private ->field-sort-builder
  [key value]
  (let [fsb (FieldSortBuilder. (name key))]
    (cond (map? value)
          (do
            (when-let [iu (:ignoreUnmapped value)]
              (.ignoreUnmapped fsb iu))
            (when-let [order (:order value)]
              (.order fsb (->sort-order order))))

          (or (instance? clojure.lang.Named value)
              (instance? String value))
          (.order fsb (->sort-order (name value))))
    fsb))

(defn ^SearchSourceBuilder ^:private set-sort
  [^SearchSourceBuilder sb sort]
  (cond
   (instance? String sort)       (.sort sb ^String sort)
   ;; Allow 'sort' to be a SortBuilder, such as a GeoDistanceSortBuilder.
   (instance? SortBuilder sort)  (.sort sb ^SortBuilder sort)
   ;; map
   :else  (doseq [[k v] sort]
            (.sort sb (->field-sort-builder k v))))
  sb)

(defn ^:private add-partial-fields-to-builder
  [^SearchSourceBuilder sb _source]
  (cond
   (nil? _source)        sb
   (false? _source)      (.fetchSource sb false)
   (map? _source)        (let [m  (wlk/stringify-keys _source)
                               in (->string-array (m "include" []))
                               ex (->string-array (m "exclude" []))]
                           (.fetchSource sb in ex))
   (sequential? _source) (.fetchSource sb (->string-array _source)
                                       (->string-array []))
   :else sb))

(defn ^HighlightBuilder$Field make-field
  [field-name {:keys [type pre_tags post_tags order
                      highlight_filter fragment_size number_of_fragments
                      encoder require_field_match boundary_max_scan
                      boundary_chars fragmenter highlight_query no_match_size
                      phrase_limit force_source] :as opts}]
  (let [fd (HighlightBuilder$Field. (name field-name))]
    (when type
      (.highlighterType fd type))
    (when pre_tags
      (.preTags fd (->string-array pre_tags)))
    (when post_tags
      (.postTags fd (->string-array post_tags)))
    (when order
      (.order fd order))
    (when highlight_filter
      (.highlightFilter fd highlight_filter))
    (when fragment_size
      (.fragmentSize fd ^{:tag "int"} fragment_size))
    (when number_of_fragments
      (.numOfFragments fd ^{:tag "int"} number_of_fragments))
    (when require_field_match
      (.requireFieldMatch fd require_field_match))
    (when boundary_max_scan
      (.boundaryMaxScan fd boundary_max_scan))
    ;; TODO: boundary_chars
    (when fragmenter
      (.fragmenter fd fragmenter))
    (when force_source
      (.forceSource fd force_source))
    ;; TODO: highlight_query
    ;; TODO: no_match_size
    ;; TODO: phrase_limit
    ;; TODO: custom highlighter options
    fd))

(defn ^:private add-highlight-to-builder
  [^SearchSourceBuilder sb {:keys [fields type tags_schema pre_tags post_tags order
                                   highlight_filter fragment_size number_of_fragments
                                   encoder require_field_match boundary_max_scan
                                   boundary_chars fragmenter highlight_query no_match_size
                                   phrase_limit force_source] :as opts}]
  (let [^HighlightBuilder hb (.highlighter sb)]
    (when type
      (.highlighterType hb type))
    (when tags_schema
      (.tagsSchema hb tags_schema))
    (when pre_tags
      (.preTags hb (->string-array pre_tags)))
    (when post_tags
      (.postTags hb (->string-array post_tags)))
    (when order
      (.order hb order))
    (when highlight_filter
      (.highlightFilter hb highlight_filter))
    (when fragment_size
      (.fragmentSize hb ^{:tag "int"} fragment_size))
    (when number_of_fragments
      (.numOfFragments hb ^{:tag "int"} number_of_fragments))
    (when encoder
      (.encoder hb encoder))
    (when require_field_match
      (.requireFieldMatch hb require_field_match))
    (when boundary_max_scan
      (.boundaryMaxScan hb boundary_max_scan))
    ;; TODO: boundary_chars
    (when fragmenter
      (.fragmenter hb fragmenter))
    (when force_source
      (.forceSource hb force_source))
    ;; TODO: highlight_query
    ;; TODO: no_match_size
    ;; TODO: phrase_limit
    ;; TODO: custom highlighter options
    (doseq [[k v] fields]
      (.field hb (make-field k v)))
    ))

(defn ^SearchRequest ->search-request
  [index-name mapping-type {:keys [search-type search_type scroll routing
                                   preference query aggregations from size timeout
                                   template params
                                   post-filter filter min-score version fields sort stats _source
                                   highlight] :as options}]
  (let [r                       (SearchRequest.)
        ^SearchSourceBuilder sb (SearchSourceBuilder.)]

    ;; source
    (when query
      (.query sb ^Map (wlk/stringify-keys query)))
    (when aggregations
      (.aggregations sb ^Map (wlk/stringify-keys aggregations)))
    (when from
      (.from sb from))
    (when size
      (.size sb size))
    (when timeout
      (.timeout sb ^String timeout))
    (when filter
      (.postFilter sb ^Map (wlk/stringify-keys filter)))
    ;; compatibility
    (when post-filter
      (.postFilter sb ^Map (wlk/stringify-keys post-filter)))
    (when fields
      (.fields sb ^java.util.List fields))
    (when _source
      (add-partial-fields-to-builder sb _source))
    (when min-score
      (.minScore sb min-score))
    (when version
      (.version sb version))
    (when sort
      (set-sort sb sort))
    (when stats
      (.stats sb (->string-array stats)))
    (when highlight
      (add-highlight-to-builder sb highlight))

    (.source r sb)

    ;; non-source
    (when index-name
      (.indices r (->string-array index-name)))
    (when mapping-type
      (.types r (->string-array mapping-type)))
    (when-let [s (or search-type search_type)]
      (.searchType r ^String s))
    (when routing
      (.routing r ^String routing))
    (when scroll
      (.scroll r ^String scroll))
    (when template
      (.templateName r ^String (:id template))
      (.templateType r ^org.elasticsearch.script.ScriptService$ScriptType org.elasticsearch.script.ScriptService$ScriptType/INDEXED))
    (when params
      (.templateParams r ^Map (wlk/stringify-keys params)))
    r))

(defn ^MultiSearchRequest ->multi-search-request
  ([^Client conn queries opts]
     (let [sb (.newRequestBuilder MultiSearchAction/INSTANCE conn)]
       ;; pairs of [{:index "index name" :type "mapping type"}, search-options]
       (doseq [[{:keys [index type]} search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb)))
  ([^Client conn ^String index queries opts]
     (let [sb (.newRequestBuilder MultiSearchAction/INSTANCE conn)]
       (doseq [[{:keys [type]} search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb)))
  ([^Client conn ^String index ^String type queries opts]
     (let [sb (.newRequestBuilder MultiSearchAction/INSTANCE conn)]
       (doseq [[_ search-opts] (partition 2 queries)]
         (.add sb (->search-request index type search-opts)))
       (.request sb))))

(defn ^SearchScrollRequest ->search-scroll-request
  [^String scroll-id {:keys [scroll]}]
  (let [r (SearchScrollRequest. scroll-id)]
    (when scroll
      (.scroll r ^String scroll))
    r))

(defn ->string
  [text]
  (if (keyword? text)
    (name text)
    (str text)))

(defn attach-suggestion-context
  "attach context for suggestion query."
  [query context]
  (let [add-category! (fn [field-name context-value]
                        (.addCategory query
                                      ^String (->string field-name)
                                      (->string-array context-value)))
        add-location! (fn [field-name {:keys [lat lon precision]}]
                        (if (empty? precision)
                          (.addGeoLocation query
                                           (->string field-name)
                                           (double lat)
                                           (double lon)
                                           nil)
                          (.addGeoLocationWithPrecision query
                                                        (->string field-name)
                                                        (double lat) (double  lon)
                                                        (->string-array precision))))]
    (doseq [[field context-dt] context]
      (cond
        (string? context-dt) (add-category! field context-dt)
        (vector? context-dt) (add-category! field context-dt)
        (map? context-dt) (when (contains? context-dt :lat)
                            (add-location! field context-dt))))
    query))

;; TODO: add builder for term suggestor
(defmulti ->suggest-query (fn [qtype _ _] qtype))
(defmethod ^CompletionSuggestionBuilder ->suggest-query :completion
  [qtype term {:keys [field size analyzer context]
               :or {field "suggest"}}]
  "builds a suggestion query object for simple autocomplete"
  (let [query (doto (CompletionSuggestionBuilder. "hits")
                (.text ^String term)
                (.field ^String field))]
    (when size (.size query size))
    (when analyzer (.analyzer query analyzer))
    (when context (attach-suggestion-context query context))
    query))

(defmethod ^CompletionSuggestionFuzzyBuilder ->suggest-query :fuzzy
  [qtype term {:keys [field size analyzer fuzziness transpositions
                      min-length prefix-length unicode-aware context]
               :or {field "suggest"
                    transpositions true
                    min-length 3
                    prefix-length 1
                    unicode-aware false}}]
  "builds query for fuzzy completion which allows little typos in search term"
  (let [fuzz-level (case fuzziness
                     0 Fuzziness/ZERO
                     1 Fuzziness/ONE
                     2 Fuzziness/TWO
                     Fuzziness/AUTO)
        query (doto (CompletionSuggestionFuzzyBuilder. "hits")
                (.text ^String term)
                (.field ^String field)
                (.setFuzziness ^Fuzziness fuzz-level)
                (.setFuzzyTranspositions ^Boolean transpositions)
                (.setFuzzyMinLength ^Integer min-length)
                (.setFuzzyPrefixLength ^Integer prefix-length)
                (.setUnicodeAware ^Boolean unicode-aware))]
    (when size (.size query size))
    (when analyzer (.analyzer query analyzer))
    (when context (attach-suggestion-context query context))
    query))

(defn ^:private highlight-field-to-map
  [^HighlightField hlf]
  {})

(defn ^:private add-highlight-from
  [^SearchHit sh m]
  (let [hls (.highlightFields sh)
        hlm (reduce (fn [acc [^String k ^HighlightField hlf]]
                      (assoc acc (keyword k) (vec (map (fn [^Text t]
                                                         (.string t)) (.getFragments hlf)))))
                    {}
                    hls)]
    (assoc m :highlight hlm)))

(defn- ^IPersistentMap search-hit->map
  [^SearchHit sh]
  (let [source (.getSource sh)
        fs (dissoc (convert-fields-result (.getFields sh)) :_source)
        result (add-highlight-from sh {:_index    (.getIndex sh)
                                       :_type     (.getType sh)
                                       :_id       (.getId sh)
                                       :_score    (.getScore sh)
                                       :_version  (.getVersion sh)})
        result-with-source (if source
                             (assoc result :_source (convert-source-result source))
                             result)]
    (if-not (or (nil? fs)
                (empty? fs))
      (assoc result-with-source
        :_fields fs
        :fields fs)
      result-with-source)))

(defn- search-hits->seq
  [^SearchHits hits]
  {:total     (.getTotalHits hits)
   :max_score (.getMaxScore hits)
   :hits      (map search-hit->map (.getHits hits))})

(defprotocol AggregatorPresenter
  (aggregation-value [agg] "Presents an aggregation as immutable Clojure map"))

(defn assoc-aggregation-value
  [acc [^String name agg]]
  ;; <String, Aggregation>
  (assoc acc (keyword name) (aggregation-value agg)))

(defn aggregations-to-map
  [^Aggregations aggs]
  (reduce assoc-aggregation-value {} (.asMap aggs)))

(defn merge-sub-aggregations [m ^HasAggregations b]
  (clojure.core/merge
   m
   (aggregations-to-map (.getAggregations b))))

(defn histogram-bucket->map
  [^Histogram$Bucket b]
  (merge-sub-aggregations
   {:key (.getKey b)
    :doc_count (.getDocCount b)}
   b))

(defn range-bucket->map
  [^Range$Bucket b]
  (merge-sub-aggregations
   {:doc_count (.getDocCount b)
    :from_as_string (String/valueOf ^{:tag "long"} (.. b getFrom longValue))
    :from (.. b getFrom)
    :to_as_string (String/valueOf ^{:tag "long"} (.. b getTo longValue))
    :to (.. b getTo)}
   b))

(defn terms-bucket->map
  [^Terms$Bucket b]
  (merge-sub-aggregations
   {:doc_count (.getDocCount b)
    :key (.getKey b)}
   b))

(extend-protocol AggregatorPresenter
  Avg
  (aggregation-value [^Avg agg]
    {:value (.getValue agg)})

  Max
  (aggregation-value [^Max agg]
    {:value (.getValue agg)})

  Min
  (aggregation-value [^Min agg]
    {:value (.getValue agg)})

  Sum
  (aggregation-value [^Sum agg]
    {:value (.getValue agg)})

  Percentiles
  (aggregation-value [^Percentiles agg]
    {:values (reduce (fn [acc ^Percentile p]
                       (assoc acc (keyword (str (.getPercent p))) (.getValue p)))
                     {}
                     agg)})

  Cardinality
  (aggregation-value [^Cardinality agg]
    {:value (.getValue agg)})

  ValueCount
  (aggregation-value [^ValueCount agg]
    {:value (.getValue agg)})

  ;; Missing, Global, etc
  SingleBucketAggregation
  (aggregation-value [^SingleBucketAggregation agg]
    (->> (.getAggregations agg)
         (aggregations-to-map)
         (clojure.core/merge {:doc_count (.getDocCount agg)})))

  Stats
  (aggregation-value [^Stats agg]
    {:count (.getCount agg)
     :min   (.getMin agg)
     :max   (.getMax agg)
     :avg   (.getAvg agg)
     :sum   (.getSum agg)})

  ExtendedStats
  (aggregation-value [^ExtendedStats agg]
    {:count (.getCount agg)
     :min   (.getMin agg)
     :max   (.getMax agg)
     :avg   (.getAvg agg)
     :sum   (.getSum agg)
     :sum_of_squares (.getSumOfSquares agg)
     :variance       (.getVariance agg)
     :std_deviation  (.getStdDeviation agg)})

  TopHits
  (aggregation-value [^TopHits agg]
    {:hits (search-hits->seq (.getHits agg))})

  Histogram
  (aggregation-value [^Histogram agg]
    {:buckets (vec (map histogram-bucket->map (.getBuckets agg)))})

  Range
  (aggregation-value [^Range agg]
    {:buckets (vec (map range-bucket->map (.getBuckets agg)))})

  Terms
  (aggregation-value [^Terms agg]
    {:buckets (vec (map terms-bucket->map (.getBuckets agg)))}))

(defn search-response->seq
  [^SearchResponse r]
  ;; Example REST API response:
  ;;
  ;; {:took 18,
  ;;  :timed_out false,
  ;;  :_shards {:total 5, :successful 5, :failed 0},
  ;;  :hits {
  ;;    :total 4,
  ;;    :max_score 1.0,
  ;;    :hits [{:_index "articles",
  ;;       :_type "article",
  ;;       :_id 1,
  ;;       :_score 1.0,
  ;;       :_source {:latest-edit {:date "2012-03-26T06:07:00", :author nil},
  ;;                 :number-of-edits 10,
  ;;                 :language "English",
  ;;                 :title "Elasticsearch",
  ;;                 :url "http://en.wikipedia.org/wiki/Elasticsearch",
  ;;                 :summary "...",
  ;;                 :tags "..." }}
  ;;       {:_index "articles",
  ;;       :_type "article",
  ;;       :_id 2,
  ;;       :_score 1.0,
  ;;       :_source {:latest-edit {:date "2012-03-11T02:19:00", :author "..." },
  ;;                 :number-of-edits 48,
  ;;                 :language "English",
  ;;                 :title "Apache Lucene",
  ;;                 :url "http://en.wikipedia.org/wiki/Apache_Lucene",
  ;;                 :summary "..." }}]
  ;;  }
  ;;  :aggregations {:avg_age {:value 29.0}}
  ;; }
  (let [m {:took       (.getTookInMillis r)
           :timed_out  (.isTimedOut r)
           :_scroll_id (.getScrollId r)
           ;; TODO: suggestions
           :_shards    {:total      (.getTotalShards r)
                        :successful (.getSuccessfulShards r)
                        :failed     (.getFailedShards r)}
           :hits       (search-hits->seq (.getHits r))}]
    (if (seq (.getAggregations r))
      (clojure.core/merge m {:aggregations (aggregations-to-map (. r getAggregations))})
      m)))

(defn suggest-response->seq
  [^SuggestResponse r]
  ;; Example REST API response
  ;;{"_shards" : {
  ;; "total" : 5,
  ;; "successful" : 5,
  ;; "failed" : 0 },
  ;; "hits" : {
  ;;  "text" : "Stock",
  ;;  "offset" : 0,
  ;;  "length" : 5,
  ;;  "options" : [ {
  ;;    "text" : "Stockby,Stockholm",
  ;;    "score" : 1.0,
  ;;    "payload":{"id":2673749,"city":"Stockby","region":"Stockholm","country":"Sweden","latitude":59.33333,"longitude":17.68333}}]}...
  (let [results (json/parse-string  (.toString r) true)]
    {:_shards {:total      (.getTotalShards r)
               :successful (.getSuccessfulShards r)
               :failed     (.getFailedShards r)}
     :hits (-> results :hits first)}))


(defn multi-search-response->seq
  [^MultiSearchResponse r]
  ;; Example REST API response:
  ;;
  ;; [{:took 1, :timed_out false, :_shards {:total 5, :successful 5, :failed 0},
  ;;   :hits {:total 4, :max_score 1.0,
  ;;          :hits [{:_index people, :_type person, :_id 4, :_score 1.0,
  ;;                  :_source {:last-name Hall, :age 29, :username estony, :first-name Tony,
  ;;                  :title Yak Shaver, :planet Earth, :biography yak/reduce all day long, :country Uruguay}}]}}
  ;;  {:took 1, :timed_out false, :_shards {:total 5, :successful 5, :failed 0},
  ;;   :hits {:total 4, :max_score 1.0,
  ;;          :hits [{:_index articles, :_type article, :_id 4, :_score 1.0,
  ;;                  :_source {:tags geografía, EEUU, historia, ciudades, Norteamérica, :title Austin, :summary "...",
  ;;                  :language Spanish, :url http://es.wikipedia.org/wiki/Austin, :number-of-edits 13002}}]}}]
  (let [xs (map (fn [^MultiSearchResponse$Item item]
                  (.getResponse item))
                r)]
    (map search-response->seq xs)))


(defn ^IPersistentMap percolate-response->map
  [^PercolateResponse r]
  {:count (.getCount r) :matches (map (fn [^PercolateResponse$Match m]
                                        (.. m getId string))
                                      (into [] (.getMatches r)))})

;;
;; Administrative Actions
;;

(defn ^IndicesExistsRequest ->index-exists-request
  [index-name]
  (IndicesExistsRequest. (->string-array index-name)))

(defn ^TypesExistsRequest ->types-exists-request
  [index-name type-name]
  (TypesExistsRequest. (->string-array index-name) (->string-array type-name)))

(defn ^CreateIndexRequest ->create-index-request
  [index-name settings mappings]
  (let [r (CreateIndexRequest. ^String index-name)
        s (wlk/stringify-keys settings)
        m (wlk/stringify-keys mappings)]
    (when settings
      (.settings r ^Map s))
    (when mappings
      (doseq [[k v] m]
        (.mapping r ^String k ^Map v)))
    r))

(defn ^DeleteIndexRequest ->delete-index-request
  ([]
     (DeleteIndexRequest. (->string-array [])))
  ([index-name]
     (DeleteIndexRequest. (->string-array index-name))))

(defn ^UpdateSettingsRequest ->update-settings-request
  [index-name settings]
  (doto (UpdateSettingsRequest. (->string-array index-name))
    (.settings ^Map (wlk/stringify-keys settings))))

(defn ^GetSettingsRequest ->get-settings-request
  [index-name]
  (doto (GetSettingsRequest.)
    (.indices (->string-array index-name))))

(defn ^IPersistentMap ->get-settings-response->map
  [^GetSettingsResponse res]
  (->> (for [^ObjectObjectCursor e (.getIndexToSettings res)]
         (let [index-name (keyword (.key e))
               settings (.getAsMap ^Settings (.value e))]
           [index-name
            {:settings
             (reduce (fn [before [k v]]
                       (let [path (->> (clojure.string/split k #"\.")
                                       (map keyword))]
                         (assoc-in before path v)))
                     {}
                     settings)}]))
       (into {})))

(defn ^GetMappingsRequest ->get-mappings-request
  ([]
     (GetMappingsRequest.))
  ([index-name ^String mapping-type]
     (doto (GetMappingsRequest.)
       (.indices (->string-array index-name))
       (.types (->string-array mapping-type)))))

(defn ^IPersistentMap get-mappings-response->map
  [^GetMappingsResponse res]
  ;; TODO: a sane way of converting ImmutableOpenMaps to Clojure maps. MK.
  (reduce (fn [acc ^ObjectObjectCursor el]
            (let [^String           k (.key el)
                  ^ImmutableOpenMap v (.value el)]
              (assoc acc (keyword k)
                     ;; to match HTTP API responses. MK.
                     {:mappings (reduce (fn [acc2 ^ObjectObjectCursor el]
                                          (let [^String          k (.key el)
                                                ^MappingMetaData v (.value el)]
                                            (assoc acc (keyword k) (deep-java-map->map (.sourceAsMap v)))))
                                        {}
                                        v)})))
          {}
          (.mappings res)))


(defn ^PutMappingRequest ->put-mapping-request
  [index-name ^String mapping-type {:keys [mapping mappings ignore_conflicts ignore-conflicts]}]
  (let [r (doto (PutMappingRequest. (->string-array index-name))
            (.type mapping-type)
            (.source ^String (json/encode (or mapping mappings))))]
    (when-let [v (or ignore_conflicts ignore-conflicts)]
      (.ignoreConflicts r v))
    r))

(defn ^OpenIndexRequest ->open-index-request
  "opens closed index or indices for search"
  [index-name]
  (OpenIndexRequest. (->string-array index-name)))

(defn ^CloseIndexRequest ->close-index-request
  "closes index or indices for updating"
  [index-name]
  (CloseIndexRequest. (->string-array index-name)))

(defn ^ForceMergeRequest ->force-merge-request
  [index-name {:keys [max-num-segments only-expunge-deletes flush]}]
  (let [ary (->string-array index-name)
        r   (ForceMergeRequest. ary)]
    (when max-num-segments
      (.maxNumSegments r ^{:tag "int"} max-num-segments))
    (when only-expunge-deletes
      (.onlyExpungeDeletes r ^{:tag "boolean"} only-expunge-deletes))
    (when flush
      (.flush r flush))
    r))

(defn ^FlushRequest ->flush-index-request
  [index-name {:keys [force wait-if-ongoing]}]
  (let [ary (->string-array index-name)
        r   (FlushRequest. ary)]
    (when force
      (.force r ^{:tag "boolean"} force))
    (when wait-if-ongoing
      (.waitIfOngoing r ^{:tag "boolean"} wait-if-ongoing))
    r))

(defn ^RefreshRequest ->refresh-index-request
  [index-name]
  (RefreshRequest. (->string-array index-name)))

(defn ^IPersistentMap shard-operation-failed-exception->map
  [^ShardOperationFailedException e]
  {:index    (.index e)
   :shard-id (.shardId e)
   :reason   (.reason e)})

(defn ^IPersistentMap acknowledged-response->map
  [^AcknowledgedResponse res]
  ;; matches REST API responses
  {:acknowledged (.isAcknowledged res)})

(defn ^IPersistentMap broadcast-operation-response->map
  [^BroadcastResponse res]
  ;; matches REST API responses
  {:_shards {:total      (.getTotalShards res)
             :successful (.getSuccessfulShards res)
             :failed     (.getFailedShards res)
             :failures   (map shard-operation-failed-exception->map (.getShardFailures res))}})


(defn ^PutRepositoryRequest ->put-repository-request
  [^String name {:keys [type settings]}]
  (let [r (PutRepositoryRequest. name)]
    (when type
      (.type r type))
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    r))

(defn ^CreateSnapshotRequest ->create-snapshot-request
  [^String repository ^String snapshot {:keys [wait-for-completion? partial settings indices]}]
  (let [r (CreateSnapshotRequest. repository snapshot)]
    (when wait-for-completion?
      (.waitForCompletion r wait-for-completion?))
    (when partial
      (.partial r partial))
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    (when indices
      (.indices r ^{:tag "[Ljava.lang.String;"} indices))
    r))

(defn ^DeleteSnapshotRequest ->delete-snapshot-request
  [^String repository ^String snapshot]
  (DeleteSnapshotRequest. repository snapshot))


(defn ^ClearIndicesCacheRequest ->clear-indices-cache-request
  [index-name {:keys [filter-cache field-data-cache id-cache fields]}]
  (let [ary (->string-array index-name)
        r   (ClearIndicesCacheRequest. ary)]
    (when filter-cache
      (.filterCache r filter-cache))
    (when field-data-cache
      (.fieldDataCache r field-data-cache))
    (when id-cache
      (.idCache r id-cache))
    (when fields
      (.fields r (->string-array fields)))
    r))


(defn ^IndicesStatsRequest ->index-stats-request
  ([]
     (let [r (IndicesStatsRequest.)]
       (.all r)
       r))
  ([{:keys [docs store indexing types groups get
            search merge flush refresh]}]
     (let [r   (IndicesStatsRequest.)]
       (.clear r)
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

(defn ^IndicesSegmentsRequest ->indices-segments-request
  [index-name]
  (IndicesSegmentsRequest. (->string-array index-name)))

(defn ^IPersistentMap index-segments->map
  [^IndexSegments segs]
  ;; TODO
  segs)

(defn ^IPersistentMap indices-segments-response->map
  [^IndicesSegmentResponse r]
  (reduce (fn [m [^String idx ^IndexSegments segs]]
            (assoc m idx (index-segments->map segs)))
          {}
          (.getIndices r)))


(defn- apply-add-alias
  [^IndicesAliasesRequest req {:keys [index indices alias aliases filter]}]
  (let [indices-array (->string-array (or indices index))
        aliases-array (->string-array (or aliases alias))]
    (doseq [alias aliases-array]
      (if filter
        (.addAlias req ^String alias ^Map filter indices-array)
        (.addAlias req ^String alias indices-array))))
  req)

(defn- apply-remove-alias
  [^IndicesAliasesRequest req {:keys [index indices alias aliases]}]
  (let [indices-array (->string-array (or indices index))
        aliases-array (->string-array (or aliases alias))]
    (doseq [index indices-array]
      (.removeAlias req ^String index aliases-array)))
  req)

(defn ^IndicesAliasesRequest ->indices-aliases-request
  [ops {:keys [timeout]}]
  (let [r (IndicesAliasesRequest.)]
    (doseq [{:keys [add remove timeout]} ops]
      (when add
        (apply-add-alias r add))
      (when remove
        (apply-remove-alias r remove)))
    (when timeout
      (.timeout r ^String timeout))
    r))

(defn ^PutIndexTemplateRequest ->put-index-template-request
  [template-name {:keys [template settings mappings order]}]
  (let [r (doto (PutIndexTemplateRequest. template-name)
            (.template template))]
    (when settings
      (.settings r ^Map (wlk/stringify-keys settings)))
    (when mappings
      (doseq [[k v] (wlk/stringify-keys mappings)]
        (.mapping r ^String k ^Map v)))
    (when order
      (.order r order))
    r))

(defn ^PutIndexTemplateRequest ->create-index-template-request
  [template-name {:as options}]
  (doto (->put-index-template-request template-name options)
    (.create)))

(defn ^DeleteIndexTemplateRequest ->delete-index-template-request
  [template-name]
  (DeleteIndexTemplateRequest. template-name))

;;
;; Bulk Responses
;;

(defn bulk-item-response->map
  [^BulkItemResponse item]
  (let [res {:index (.getIndex item)
             :_index (.getIndex item)
             :type (.getType item)
             :_type (.getType item)
             :id (.getId item)
             :_id (.getId item)
             :version (.getVersion item)
             :_version (.getVersion item)
             :op-type (.getOpType item)
             :failed? (.isFailed item)}]
    (if (:failed? res)
      (assoc res :failure-message (.getFailureMessage item))
      res)))

(defn bulk-response->map
  [^BulkResponse response]
  {:took (.getTookInMillis response)
   :has-failures? (.hasFailures response)
   :items (mapv bulk-item-response->map (.getItems response))})

(defn remove-underscores
  [opts]
  (reduce-kv (fn [m k v]
               (assoc m (keyword (clojure.string/replace (name k) #"^_" "")) v))
             {} opts))

(defn get-bulk-item-action
  [doc]
  (cond (contains? doc "index") "index"
        (contains? doc "update") "update"
        (contains? doc "delete") "delete"
        :else nil))

(defn ->action-requests
  [a]
  (loop [actions a
         results []]
    (let [curr (first actions)
          request-type (get-bulk-item-action curr)
          add (case request-type
                "update" (let [source (second actions)
                               opts (clojure.core/get curr "update")]
                           (->update-request
                             (:_index opts)
                             (:_type opts)
                             (:_id opts)
                             source
                             (remove-underscores opts)))
                "index" (let [source (second actions)
                              opts (clojure.core/get curr "index")]
                          (->index-request
                           (:_index opts)
                           (:_type opts)
                           source
                           (remove-underscores opts)))
                "delete" (let [opts (clojure.core/get curr "delete")]
                           (->delete-request
                            (:_index opts)
                            (:_type opts)
                            (:_id opts)
                            (remove-underscores opts)))
                nil nil)
          new-results (if (nil? add) results (conj results add))
          next-rest (case request-type
                      "index" (rest (rest actions))
                      "update" (rest (rest actions))
                      "delete" (rest actions)
                      nil ())]
      (if (empty? next-rest)
        new-results
        (recur next-rest new-results)))))
