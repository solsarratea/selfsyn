(defproject selfsyn "0.1.0-SNAPSHOT"
  :description "Finding SELF in Synthesing in Shaders and Tones"
  :url ""
  :license
  {:name "MIT"
   :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [overtone "e925f06d25786ad9b3b9fddcd87eb2a11f359762"]
                 [shadertone "89d3a647d18e95fce4f90fff3feb5d8af2522575"]
                 [clj-native "62c99c4bdaedd4750420c8e29c7a61bc9d8aa5c0"]
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
