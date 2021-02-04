(ns selfsyn.core
  (:use [selfsyn.chow]
        [selfsyn.audio]
        [selfsyn.synth]
        [selfsyn.sched]
        [overtone.live])
  (:require [shadertone.tone :as t]))

(defn disconnect
  "Disconnect from SuperCollider"
  []
  (kill-server))

(def title "selfsyn-output")

(defn julia
  "Starts Julia Set"
  []
  (t/start "resources/shaders/julia.glsl"
           :title title
           :width 1600 :height 900
           :textures [:overtone-audio :previous-frame]))

(defn simple
  ""
  []
  (t/start "resources/shaders/simple.glsl"
           :title "simp"
           :width 1600 :height 900
           :textures [:overtone-audio :previous-frame]))

(defn marble
  ""
  []
  (t/start "resources/shaders/marble.glsl"
           :title "mar"
           :width 1600 :height 900
           :textures [:overtone-audio :previous-frame]))

(defn ray
  "Rey de las marchas"
  []
  (t/start "resources/shaders/field.glsl"
           :title "mar"
           :width 1600 :height 900
           :textures [:overtone-audio :previous-frame]))

(ray)

(defn vvv
  []
  (t/start "resources/shaders/vvv.glsl"
           :title "mar"
           :width 1600 :height 900
           :textures [:overtone-audio :previous-frame]))

(defn -main
  [& args]
  ;FIXME;
)
