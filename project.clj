(defproject selfsyn "0.1.0-SNAPSHOT"
  :description "Finding SELF in Synthesing in Shaders and Tones"
  :url ""
  :license
  {:name "MIT"
   :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [overtone "0.10.7-SNAPSHOT" :exclusions [[clj-native]]]
                 [shadertone "0.2.6-SNAPSHOT"]
                 [clj-native "0.9.6"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/tools.namespace "0.3.1"]
                 [mud "0.1.2-SNAPSHOT"]
                 [overtone.synths "0.1.0-SNAPSHOT"]
                 [overtone.orchestra "0.1.0-SNAPSHOT"]
                  [org.clojure/math.numeric-tower "0.0.4"]
                 ]
  :main ^:skip-aot selfsyn.core
  :repl-options {:init-ns solquemal
                 :host "127.0.0.1"
                 :port 47480}
  :jvm-opts ^replace [])
