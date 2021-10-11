(defproject selfsyn "0.1.0-SNAPSHOT"
  :description "Finding SELF in Synthesing in Shaders and Tones"
  :url ""
  :license
  {:name "MIT"
   :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [overtone "59d29497bfd0242a423c4c1797241e6513a891d2" :exclusions [[clj-native]]]
                 [shadertone "86012996a9ac4036b494f3894309cbe8a15ec486"]
                 [clj-native "6b75d4a59cf85779c3935ff55330bed68da92124"]
                 [org.clojure/core.match "1.0.0"]
                 [org.clojure/tools.namespace "0.3.1"]
                 [mud "0.1.2-SNAPSHOT"]
                 [overtone.synths "0.1.0-SNAPSHOT"]
                 [overtone.orchestra "0.1.0-SNAPSHOT"]
                  [org.clojure/math.numeric-tower "0.0.4"]
                 ]
  :plugins [[reifyhealth/lein-git-down "0.4.0"]]
  :middleware [lein-git-down.plugin/inject-properties]
  :repositories [["public-github" {:url "git://github.com"}]]
  :git-down {overtone {:coordinates markus-wa/overtone}
             clj-native {:coordinates markus-wa/clj-native}
             shadertone {:coordinates markus-wa/shadertone}}
  :main ^:skip-aot selfsyn.core
  :repl-options {:init-ns solquemal
                 :host "127.0.0.1"
                 :port 47480}
  :jvm-opts ^replace [])
