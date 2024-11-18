---
title: "Elastisch, a minimalistic Clojure client for ElasticSearch: Suggestions"
layout: article
---

## About this guide

This guide covers ElasticSearch [suggestion
feature](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html),
explains how Elastisch presents them in the API and how some of the
key features are commonly used. This guide covers:

 * An overview of ElasticSearch suggestion
 * How to perform suggestion queries with Elastisch
  	* simple completion
  	* fuzzy completion
  	 	
 * Context suggesters
 	* category context
 	* geofiltering 	

This work is licensed under a <a rel="license"
href="http://creativecommons.org/licenses/by/3.0/">Creative Commons
Attribution 3.0 Unported License</a> (including images &
stylesheets). The source is available [on
Github](https://github.com/clojurewerkz/elastisch.docs).


## What version of Elastisch does this guide cover?

This guide covers Elastisch 2.1.x releases, including preview releases. Requires ElasticSearch >1.7.1.


## Overview

The suggest feature suggests similar looking terms based on a provided text by using a suggester. Parts of the suggest feature are still under development. 

At the moment **Elastisch** allow you use simple *completion* and *fuzzy completion* with category and location filtering.


## Completion suggester

The completion suggester is a so-called prefix suggester. It does not do spell correction like the term or phrase suggesters but allows basic auto-complete functionality.

Supported options for Completion suggester:

 * *field* - a string, from what field to fetch the candidate suggestions from, by default it's *suggest*; 
 * *size* - an integer, the maximum suggestions to be returned per suggest text term, default 5;
 * *analyzer* - a string, the analyzer to analyse to suggest text with, *[simple, standard, ...]*
 * *context* -  context options for filtering by categories or geolocations, check context suggesters below.

Example:

```clojure
(require '[clojurewerkz.elastisch.native.document :as doc])

;; mapping for suggestion
(def people-suggestion-mapping
	{:person {:properties {:username {:type "string"}
                          :suggest {:type "completion"
                                    :index_analyzer "simple"
                                    :search_analyzer "simple"
                                    :payloads true}}}})
                                    
;; create an index with mapping for autocompletion
(idx/create conn index-name :mappings people-suggestion-mapping)

;; seeds suggestion data
(doc/put conn index-name "person" "1"
			{:username "esjack"
			 :suggest {:input "esjack"
           			   :output (:username person-jack)
           			   :payload {:id "jack-1"}}}))

;; get suggestions
(doc/suggest conn index-name :completion "esj" {})
(doc/suggest conn index-name :completion "es" {:field "suggest"}))

```

read more about a Completion suggester on  the [Elasticsearch documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html)

## Fuzzy completion suggester

The completion suggester also supports fuzzy queries - this means, you can actually have a typo in your search and still get results back.

Example: 

```

;; get suggestions
(doc/suggest conn index-name :fuzzy "es" {:fuzziness 1 :min-length 2})

```

Options map accepts keys:

* field, [string, default: "suggest"] - Sets from what field to fetch the candidate suggestions from.
* size, [integer, default: 5] - Sets the maximum suggestions to be returned per suggest text term
* analyzer, [string, (*simple, standard, ...*)] - Sets the analyzer to analyse to suggest text with.
* fuzziness, [integer or :auto, default: :auto] - Sets the level of fuzziness used to create suggestions .
* transpositions, [boolean, default: true] - Sets if transpositions (*swapping one character for another*) counts as one character change or two
* min-length, [integer, default: 3] - Sets the minimum length of input string before fuzzy suggestions are returned.
* prefix-length [integer, default: 1] - Sets the minimum length of the input, which is not checked for fuzzy alternatives.
* unicode-aware [boolean, default: false] - Set to true if all measurements (*like edit distance, transpositions and lengths*) are in unicode code points (actual letters) instead of bytes
* context [hash-map] - context options for filtering by categories or geolocations, check context suggesters below.


read more about a Fuzzy completion on the [Elasticsearch documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html#fuzzy)

## A context suggester

The context suggester is an extension to the suggest API of Elasticsearch. Namely the suggester system provides a very fast way of searching documents by handling these entirely in memory.

A context is defined by a set of context mappings which can either be a simple category or a geo location. The information used by the context suggester is configured in the type mapping with the context parameter, which lists all of the contexts that need to be specified in each document and in each suggestion reques

#### Category filtering with a Context suggester


The category context allows you to specify one or more categories in the document at index time. The document will be assigned to each named category, which can then be queried later. 


```clojure

;; a mapping for category filtering
(def people-suggestion-gender-context-mapping
	{:person {:properties {:username {:type "string"}
                          :suggest {:type "completion"
                                    :index_analyzer "simple"
                                    :search_analyzer "simple"
                                    :payloads true
                                    :context {:gender {:type "category"
                                                       :default ["male" "female"]}}}}}})
                                                       
;; feed a record with category
(doc/put conn index-name mapping-type "1"
	{:username "esjack"
    :suggest {:input "esjack"
              :output "esjack"
              :payload {:id "jack-1"}
	    		 :context {:gender "male"}}})
	    		 
;; find suggestions for all males starting with e
(doc/suggest conn index-name :completion "e" {:context {:gender "male"}})

```

#### Using geolocation with a Context suggester

A geo context allows you to limit results to those that lie within a certain distance of a specified geolocation. At index time, a lat/long geo point is converted into a geohash of a certain precision, which provides the context.

```clojure

;; create a mapping with geolocation context
(def people-suggestion-location-context-mapping
  {:person {:properties {:username {:type "string"}
                         :suggest {:type "completion"
                                   :index_analyzer "simple"
                                   :search_analyzer "simple"
                                   :payloads true
                                   :context {:location {:type "geo"
                                                        :precision ["100km"]
                                                        :neighbors true
                                                        :default {:lat 0.0
                                                                  :lon 0.0}}}}}}})


;; create a new documents with geolocations
(doc/put conn index-name mapping-type "1"
	{:username "esjack"
    :suggest {:input "esjack"
              :output "esjack"
              :payload {:id "jack-1"}
	    		 :context {:location {:lat 0 :lon -90}}}})
	    		 
;; find a nearest suggestion
(def opts {:context {:location {:lat 0.23 :lon -90.56}}})
(doc/suggest conn index-name :fuzzy "es" opts)
	    		 
```

## What to Read Next

The documentation is organized as [a number of
guides](/articles/guides.html)