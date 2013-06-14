(ns clojurewerkz.elastisch.query
  (:refer-clojure :exclude [range]))

(defn term
  "Term Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/term-query.html"
  [key values & { :as options }]
  (merge { (if (coll? values) :terms :term) (hash-map key values) }
         options))

(defn range
  "Range Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/range-query.html"
  [key & { :as options}]
  {:range (hash-map key options) })

(defn match
  "Match Query, before 0.19.9 known as Text Query.

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/match-query.html"
  [field query & { :as options}]
  {:match {field (merge { :query query } options) }})

(defn text
  "Match Query, before 0.19.9 known as Text Query.

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/match-query.html"
  [field query & { :as options}]
  ;; use :text for now for backwards compatibility. MK.
  {:text {field (merge { :query query } options) }})

(defn bool
  "Boolean Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/bool-query.html"
  [& { :as options}]
  {:bool options})


(defn boosting
  "Boosting Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/boosting-query.html"
  [& { :as options}]
  {:boosting options})

(defn ids
  "IDs Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/ids-query.html"
  [type ids]
  {:ids { :type type :values ids }})

(defn custom-score
  "Custom Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/custom-score-query.html"
  [& {:as options}]
  {:custom_score options})

(defn custom-boost-factor
  "Custom Boost Factor

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/custom-boost-factor-query/"
  [factor query]
  {:custom_boost_factor {:query query
                         :boost_factor factor}})

(defn constant-score
  "Constant Score Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/constant-score-query.html"
  [& {:as options}]
  {:constant_score options})

(defn dis-max
  "Dis Max Query

  For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/dis-max-query.html"
  [& {:as options}]
  {:dis_max options})

(defn prefix
  "Prefix query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/prefix-query.html"
  [& {:as options}]
  {:prefix options})

(defn field
  "Field query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/field-query.html"
  [& {:as options}]
  {:field options})

(defn filtered
  "Filtered query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/filtered-query.html"
  [& {:as options}]
  {:filtered options})

(defn fuzzy-like-this
  "FLT (fuzzy like this) query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/flt-query.html"
  [& {:as options}]
  {:fuzzy_like_this options})

(def flt fuzzy-like-this)

(defn fuzzy-like-this-field
  "FLT (fuzzy like this) query for a single field

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/flt-field-query.html"
  [& {:as options}]
  {:fuzzy_like_this_field options})

(def flt-field fuzzy-like-this-field)

(defn fuzzy
  "Fuzzy or Levenshtein (edit distance) query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/fuzzy-query.html"
  [& {:as options}]
  {:fuzzy options})

(defn match-all
  "Match All query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/match-all-query.html"
  ([]
     {:match_all {}})
  ([& {:as options}]
     {:match_all options}))

(defn more-like-this
  "MLT (More Like This) query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/mlt-query.html"
  [& {:as options}]
  {:more_like_this options})

(def mlt more-like-this)

(defn more-like-this-field
  "MLT (More Like This) query that works for a single field

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/mlt-field-query.html"
  [& {:as options}]
  {:more_like_this_field options})

(def mlt-field more-like-this-field)

(defn query-string
  "Query String query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/query-string-query.html"
  [& {:as options}]
  {:query_string options})

(defn span-first
  "Span First query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/span-first-query.html"
  [& {:as options}]
  {:span_first options})

(defn span-near
  "Span Near query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/span-near-query.html"
  [& {:as options}]
  {:span_near options})

(defn span-not
  "Span Not query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/span-not-query.html"
  [& {:as options}]
  {:span_not options})

(defn span-or
  "Span Or query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/span-or-query.html"
  [& {:as options}]
  {:span_or options})

(defn span-term
  "Span Term query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/span-term-query.html"
  [& {:as options}]
  {:span_term options})

(defn wildcard
  "Wildcard query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/wildcard-query.html"
  [& {:as options}]
  {:wildcard options})

(defn indices
  "Indices query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/indices-query.html"
  [& {:as options}]
  {:indices options})

(defn has-child
  "Has Child query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/has-child-query.html"
  [& {:as options}]
  {:has_child options})

(defn custom-filters-score
  "Custom Filters Score query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/custom-filters-score-query.html"
  [& {:as options}]
  {:custom_filters_score options})

(defn top-children
  "Top children query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/top-children-query.html"
  [& {:as options}]
  {:top_children options})

(defn nested
  "Nested document query

   For more information, please refer to http://www.elasticsearch.org/guide/reference/query-dsl/nested-query.html"
  [& {:as options}]
  {:nested options})
