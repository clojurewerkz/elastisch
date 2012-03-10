(defproject clojurewerkz/elastisch "1.0.0-SNAPSHOT"
  :description "Clojure ElasticSearch client"
  :min-lein-version "2.0.0"  
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [clj-http "0.3.3"]]
  :profiles     {
                 :all { :dependencies [[org.clojure/data.json "0.1.2"]
                                       [clj-http "0.3.3"]] }
                 :dev { :resource-paths ["test/resources"] }
                 :1.4 { :dependencies [[org.clojure/clojure "1.4.0-beta4"]]
                       :resource-paths ["test/resources"] }
                 }
  :repositories {"clojure-releases" "http://build.clojure.org/releases",
                 "sonatype"         { :url "http://oss.sonatype.org/content/repositories/releases",
                                     :snapshots false,
                                     :releases {:checksum :fail, :update :always}}}
  :warn-on-reflection true)
