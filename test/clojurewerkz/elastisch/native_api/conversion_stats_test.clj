;; Copyright (c) 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.conversion-stats-test
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native
              [document :as doc]
              [index :as idx]
              [response :refer [acknowledged?]]
              [conversion :as cnv]
              [conversion-stats :as cnv-stats]]
            [clojurewerkz.elastisch.fixtures      :as fx]
            [clojurewerkz.elastisch.test.helpers  :as th]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes
                    fx/prepopulate-people-index
                    fx/prepopulate-articles-index
                    fx/prepopulate-tweets-index)

(def es-conn (th/connect-native-client))

(defn get-raw-stats
  "returns IndicesStatsResponse class without any conversion"
  [es-conn]
  (.actionGet
      (es/admin-index-stats es-conn (cnv/->index-stats-request {}))))

(deftest ^{:indexing true :native true} test-to-stats-for-nils
  (let [s nil]
    (is (map? (cnv-stats/to-stats s)))
    (is (empty? (cnv-stats/to-stats s)))))

(deftest ^{:indexing true :native true} test-to-stats-for-docStats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getDocs cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :count))
    (is (pos? (:count res)))
    (is (contains? res :deleted))))

(deftest ^{:indexing true :native true} test-to-stats-for-completion-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getCompletion cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :size_in_bytes))
    (is (number? (:size_in_bytes res)))))

(deftest ^{:indexing true :native true} test-to-stats-for-field-data
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getFieldData cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :memory_size_in_bytes))
    (is (number? (:memory_size_in_bytes res)))
    (is (contains? res :evictions))
    (is (number? (:evictions res)))))

(deftest ^{:indexing true :native true} test-to-stats-for-flush
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getFlush cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total))
    (is (number? (:total res)))
    (is (contains? res :total_time_in_millis))
    (is (number? (:total_time_in_millis res)))))

(deftest ^{:indexing true :native true} test-to-stats-for-get
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getGet cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total))
    (is (number? (:total res)))
    (is (contains? res :exists_total))
    (is (number? (:exists_total res)))
    (is (contains? res :missing_total))
    (is (number? (:missing_total res)))
    (is (contains? res :missing_time_in_millis))
    (is (number? (:missing_time_in_millis res)))
    (is (contains? res :current))
    (is (number? (:current res)))))

(deftest ^{:indexing true :native true} test-to-stats-for-indexing-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getIndexing cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :index_total))
    (is (number? (:index_total res)))
    (is (contains? res :index_time_in_millis))
    (is (contains? res :index_current))
    (is (contains? res :index_failed))
    (is (contains? res :delete_total))
    (is (contains? res :delete_time_in_millis))
    (is (contains? res :delete_current))
    (is (contains? res :noop_update_total))
    (is (contains? res :is_throttled))
    (is (contains? res :throttle_time_in_millis))))

(deftest ^{:indexing true :native true} test-to-stats-for-merge-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getMerge cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :current))
    (is (contains? res :current_docs))
    (is (contains? res :current_size_in_bytes))
    (is (contains? res :total))
    (is (contains? res :total_time_in_millis))
    (is (contains? res :total_docs))
    (is (contains? res :total_size_in_bytes))
    (is (contains? res :total_stopped_time_in_millis))
    (is (contains? res :total_throttled_time_in_millis))
    (is (contains? res :total_auto_throttle_in_bytes))))

(deftest ^{:indexing true :native true} test-to-stats-for-percolate
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getPercolate cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total))
    (is (contains? res :time_in_millis))
    (is (contains? res :memory_size_in_bytes))
    (is (contains? res :memory_size))
    (is (contains? res :queries))))

(deftest ^{:indexing true :native true} test-to-stats-for-query-cache
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getQueryCache cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total_count))
    (is (contains? res :hit_count))
    (is (contains? res :memory_size_in_bytes))
    (is (contains? res :miss_count))
    (is (contains? res :cache_size))
    (is (contains? res :cache_count))
    (is (contains? res :evictions))))

(deftest ^{:indexing true :native true} test-to-stats-for-recovery-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getRecoveryStats cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :current_as_source))
    (is (contains? res :current_as_target))
    (is (contains? res :throttle_time_in_millis))))

(deftest ^{:indexing true :native true} test-to-stats-for-refresh-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getRefresh cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total))
    (is (contains? res :total_time_in_millis))))

(deftest ^{:indexing true :native true} test-to-stats-for-request-cache-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getRequestCache cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :memory_size_in_bytes))
    (is (contains? res :evictions))
    (is (contains? res :hit_count))
    (is (contains? res :miss_count))))

(deftest ^{:indexing true :native true} test-to-stats-for-search
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getSearch cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :open_contexts))
    (is (contains? res :query_total))
    (is (contains? res :query_time_in_millis))
    (is (contains? res :query_current))
    (is (contains? res :fetch_total))
    (is (contains? res :fetch_time_in_millis))
    (is (contains? res :fetch_current))
    (is (contains? res :scroll_total))
    (is (contains? res :scroll_time_in_millis))
    (is (contains? res :scroll_current))))

(deftest ^{:indexing true :native true} test-to-stats-for-segments
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getSegments cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :count))
    (is (contains? res :memory_in_bytes))
    (is (contains? res :terms_memory_in_bytes))
    (is (contains? res :stored_fields_memory_in_bytes))
    (is (contains? res :term_vectors_memory_in_bytes))
    (is (contains? res :norms_memory_in_bytes))
    (is (contains? res :doc_values_memory_in_bytes))
    (is (contains? res :index_writer_memory_in_bytes))
    (is (contains? res :index_writer_max_memory_in_bytes))
    (is (contains? res :version_map_memory_in_bytes))
    (is (contains? res :fixed_bit_set_memory_in_bytes))))

(deftest ^{:indexing true :native true} test-to-stats-for-suggest
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getSuggest cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :total))
    (is (contains? res :time_in_millis))
    (is (contains? res :current))))

(deftest ^{:indexing true :native true} test-to-stats-for-store
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getStore cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :size_in_bytes))
    (is (contains? res :throttle_time_in_millis))))

(deftest ^{:indexing true :native true} test-to-stats-for-translog
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getTranslog cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :operations))
    (is (contains? res :size_in_bytes))))

(deftest ^{:indexing true :native true} test-to-stats-for-warmer-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries .getWarmer cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :current))
    (is (contains? res :total))
    (is (contains? res :total_time_in_millis))))

(deftest ^{:indexing true :native true} test-to-stats-for-common-stats
  (let [s (get-raw-stats es-conn)
        res (-> s .getPrimaries cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :completion))
    (is (contains? res :docs))
    (is (contains? res :fielddata))))


(deftest ^{:indexing true :native true} test-to-stats-for-common-stats
  (let [s (get-raw-stats es-conn)
        res (-> s cnv-stats/to-stats)]
    (is (map? res))
    (is (contains? res :_all))
    (is (contains? res :indices))
    (is (empty? (clojure.set/difference #{"tweets" "people" "articles"}
           (keys (get res :indices {})))))))

