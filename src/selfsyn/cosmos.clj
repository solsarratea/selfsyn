(ns selfsyn.cosmos
    (:use [overtone.live]
          [mud.core]
          [selfsyn.synth :refer [voz-buffer]]
          [selfsyn.cosmoshelper]
          [selfsyn.wavs :refer [efficient-seqer seqer] :as s])
    (:require [mud.timing :as time]
              [overtone.studio.fx :as fx]
              [shadertone.tone :as t])
    (:require [overtone.algo.euclidean-rhythm :refer [euclidean-rhythm]]))


;;;;;;;;;;;;;;;;;;;;;;
;; COSMOS M A G I C ;;
;;;;;;;;;;;;;;;;;;;;;;
(comment
  (def beats (buffer->tap :beat-buf kick-seq)) ;check
  {:user-data { "iBeat" (atom
                         {:synth beats :tap "beat"})}})
(comment
    (stop)
    (fadeout-master)

    (repl-player (find-sample "play" 11) :rate -1.0)

    (t/start-fullscreen "resources/shaders/field.glsl"
                        :textures [:overtone-audio :previous-frame
                                   "resources/images/tex16.png"
                                   "resources/images/tex09.jpg"
                                   ])

    (t/stop)
    )

(stop)
(do
  (defonce note-buf (buffer 256))
  (pattern! note-buf (map note [:C#4 :E3 0 0  :B3 :D4  0 :D4  :A4 :C#4, 0, :c#4]))
  (pattern! note-buf
            (repeat 8 (degrees-seq [:f#3 5 7 _ _]))
            (repeat 8 (degrees-seq [:f#3 4 6 _ 6]))
            (repeat 8 (degrees-seq [:f#3 3 5 _ 5]))
            (repeat 8 (degrees-seq [:f#3 2 3 _ 7]))
            (repeat 8 (degrees-seq [:f#3 1 3 _ 5]))
            (repeat 8 (degrees-seq [:f#3 1 3 6 5]))))
(do
  (defonce bass-buf (buffer 256))
  (pattern! bass-buf
            (repeat 8 (degrees-seq [:f#1 1 _ ]))
            (repeat 8 (degrees-seq [:f#1 6 _ ]))
            (repeat 8 (degrees-seq [:f#1 5 _ ]))
            (repeat 8 (degrees-seq [:f#1 5 _ ]))
            (repeat 8 (degrees-seq [:f#1 3 _ ]))
            (repeat 8 (degrees-seq [:f#1 5 _ ]))))

(ctl-global-clock 10.0)

(def pluk  (plucked :note-buf note-buf :beat-bus (:count time/beat-2th) :beat-trg-bus (:beat time/beat-4th) :amp 0.01 ))
(ctl pluk  :attack 0.1   :sustain 0.3   :release 0.1 :volume 0.2 :amp 0.05)

(def pluk2 (bass :note-buf note-buf :beat-bus (:count time/beat-4th) :beat-trg-bus (:beat time/beat-4th)))
(ctl pluk2 :attack 0.25 :sustain 1.   :release 0.5 :amp 0.1 :volume 0.2)

(kill pluk pluk2)

(defonce bass-buf2 (buffer 256))
 (pattern! bass-buf2
            (repeat 8 (degrees-seq [:f#4 _ _ _ 1 ]))
            (degrees-seq [:g#3 1 1 _ :f#3 4 ])
            (repeat 2 (degrees-seq [:f#4 1 1 _ 2 ]))
            (degrees-seq [:a#3 _ _ _ :f#4 1 ])
            )

(ctl-global-clock 4.0)
(stop)

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; D R U M E F F E C T S ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;
(do
  (def drum-sample (freesound "15853" ))
  (def snare-sample (freesound "338284"))
  (defonce drum-effects-g (group "drum effects")))

;;;;;;;;;;;;; Kick  ;;;;;;;;;;;;

(def kick-sample drum-sample)

(defonce effects-seq-buf (buffer 256))
(pattern! effects-seq-buf  (repeat 7 [0 1 0 0 1 0 1 0]))

(defonce effects2-seq-buf (buffer 256))
(pattern! effects2-seq-buf (repeat 14 [0 0 1 0 0 1 0 1 0]))

(def snare-organ-seq1 (efficient-seqer [:head drum-effects-g] :buf kick-sample :pattern effects-seq-buf :rate-start 1.0 :rate-limit 1.0 :amp 1.))
(ctl snare-organ-seq1 :rate-start 0.2)

(def snare-organ-seq2 (efficient-seqer [:head drum-effects-g] :buf kick-sample :pattern effects2-seq-buf :rate-start 1. :rate-limit 0.8 :amp 0.2))
(ctl snare-organ-seq2 :amp 0.85)

(stop)

;;;;;;;;;;;;;; Snare    ;;;;;;;;;;;;;;;;
(def snare-sample snare-sample)

(defonce effects3-seq-buf (buffer 256))
(pattern! effects3-seq-buf (euclidean-rhythm 5 8))
 ;(pattern! effects3-seq-buf   [[1 1 1] [1 1] 1 0 1 0 0    0 0 0 0 0 0 0 0])

(def snare-organ-seq3 (efficient-seqer [:head drum-effects-g] :buf snare-sample :pattern effects3-seq-buf :rate-start 1.0 :rate-limit 1.0 :amp 0.9))

(ctl snare-organ-seq3 :amp 0.2)

(defonce effects4-seq-buf  (buffer 256))
(pattern! effects4-seq-buf (euclidean-rhythm 3 8));check rythms

(def snare-organ-seq4 (efficient-seqer [:head drum-effects-g] :buf snare-sample :pattern effects4-seq-buf :rate-start 0.95 :rate-limit 0.8 :amp 1.))

(ctl snare-organ-seq4 :amp 0.9)

;(ctl drum-effects-g :amp 1.0)


(def v1 ((voz-buffer
            "yo soy verdadero, pero mi verdad transciende este universo"
          ) :rate 0.95 :loop? true :out-bus 0))
(ctl v1 :rate 1.4 :amp 0.1 :note 2)

(def v2 ((voz-buffer
          "yo no soy verdadero, pero mi verdad transciende este universo"
          ) :rate 1. :loop? true :out-bus 0))
(ctl v2 :rate 1.6 :amp 0.1 :note 80)

(def v3 ((voz-buffer
          "mi verdad transciende este universo"
          ) :rate 0.95 :loop? true :out-bus 0))
(ctl v3 :rate 1.2 :amp 0.1 :note 2)

(kill v1)

(remove-all-beat-triggers)

(do

  (defonce godzilla-s  (freesound-sample 206078))
  (defonce click-s    (freesound-sample 406))
  (defonce shaker-s  (freesound-sample 100008))
  (defonce cha-s (freesound 228641))
  (defonce play-s (load-sample "resources/samples/play.wav"))
  (defonce bob-s (load-sample "resources/samples/bob.wav"))
  (defonce trololo-s (load-sample "resources/samples/trololo.wav"))
  (defonce carl-s (load-sample "resources/samples/carl.wav"))
  (defonce eight-s (freesound 392465))
  (defonce acidon-s (freesound 20521))
  (defonce space-s (freesound 396625))
  (defonce button-s (freesound 492398))
  (defonce hat-s (freesound-sample 73566))
  (defonce icq-s (freesound 556334)))

(on-beat-trigger 128 #(do (icq-s)))

(on-beat-trigger 128 #(do (button-s)))



(defonce hs (buffer 256))
(def hats (doall (map #(seqer :beat-num %1 :pattern hs :num-steps 8 :amp 0.3 :buf hat-s :rate-start 0.9) (range 0 8))))
(pattern! hs
          [1 1 0 0 0 1 0 0]
          [1 0 0 0 0 1 0 0]
          [1 0 0 0 0 1 0 0])


(defonce ambs (buffer 256))
(def hats (doall (map #(seqer :beat-num %1 :pattern hs :num-steps 8 :amp 0.3 :buf space-s :rate-start 0.9) (range 0 8))))
(pattern! ambs
          [1 1 0 0 0 1 0 0]
          [1 0 0 0 0 1 0 0]
          [1 0 0 0 0 1 0 0])



;;;;;;;;;;;;;;;;;;;;;;;;;
;; THE E N D           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;
(do (def windy (sample (freesound-path 17553)))

    (defonce rhythm-g (group "Rhythm" :after time/timing-g))
    (defonce saw-bf1 (buffer 256))
    (defonce saw-bf2 (buffer 256))

    (defonce saw-x-b1 (control-bus 1 "Timing Saw 1"))
    (defonce saw-x-b2 (control-bus 1 "Timing Saw 2"))
    (defonce saw-x-b3 (control-bus 1 "Timing Saw 3"))

    (defonce phasor-b1 (control-bus 1 "Timing Saw Phasor 1"))
    (defonce phasor-b2 (control-bus 1 "Timing Saw Phasor 2"))

    (defonce phasor-b3 (control-bus 1 "Timing Saw Phasor 3"))
    (defonce phasor-b4 (control-bus 1 "Timing Saw Phasor 4"))
    (defonce phasor-b5 (control-bus 1 "Timing Saw Phasor 5"))

    (defonce saw-s1 (time/saw-x [:head rhythm-g] :out-bus saw-x-b1))
    (defonce saw-s2 (time/saw-x [:head rhythm-g] :out-bus saw-x-b2))

    (defonce saw-s3 (time/saw-x [:head rhythm-g] :out-bus saw-x-b3))

    (defonce phasor-s1 (time/buf-phasor [:after saw-s1] saw-x-b1 :out-bus phasor-b1 :buf saw-bf1))
    (defonce phasor-s2 (time/buf-phasor [:after saw-s2] saw-x-b2 :out-bus phasor-b2 :buf saw-bf2))

    (def space-notes-buf (buffer 5))
    (def space-tones-buf (buffer 3))

    (defonce phasor-s3 (time/buf-phasor [:after saw-s3] saw-x-b3 :out-bus phasor-b3 :buf space-notes-buf)))

(defsynth buffered-plain-space-organ [out-bus 0 duration 4 amp 1]
  (let [tone (/ (in:kr phasor-b2) 2)
        tones (map #(blip (* % 2) (mul-add:kr (lf-noise1:kr 1/8) 1 4)) [tone])]
    (out out-bus (pan2 (* amp (g-verb (sum tones) 200 8))))))

  (defsynth ratatat [out-bus 0 amp 1]
    (let [freq (in:kr phasor-b2)
          sin1 (sin-osc (* 1.01 freq))
          sin2 (sin-osc (* 1 freq))
          sin3 (sin-osc (* 0.99 freq))
          src (mix [sin1 sin2 sin3])
          src (g-verb src :spread 10)]
      (out out-bus (* amp  (pan2  src)))))

  (defn transpose [updown notes]
    (map #(+ updown %1) notes))

  (def space-notes [8 16 32 16 8])
  (def space-tones [8 16 24])

  (defsynth crystal-space-organ [out-bus 0 amp 1 size 200 r 8 numharm 0 trig 0 t0 8 t1 16 t2 24 d0 1 d1 1/2 d2 1/4 d3 1/8]
    (let [notes (map  #(midicps (duty:kr % (mod trig 16) (dseq space-notes INF))) [d0 d1 d2 d3])
          tones (map (fn [note tone] (blip (* note tone) numharm)) notes [t0 t1 t2])]
      (out out-bus (* amp (g-verb (sum tones) size r)))))

(comment
  (def csp  (crystal-space-organ :numharm 0 :amp 0.5))
  )

;;Rythm

(def score   (map note [:F5 :G5 :G5 :G5 :G5 :BB5 :BB5 :D#5]))

(buffer-write! saw-bf2 (repeat 256 (midi->hz (note :A3))))
(buffer-write! saw-bf2 (map midi->hz
                            (map (fn [midi-note] (+ -12 midi-note))
                                 (map note (take 256 (cycle score))))))

(buffer-write! saw-bf2 (map midi->hz
                            (map (fn [midi-note] (+ -5 midi-note))
                                 (map note (take 256 (cycle score))))))

(buffer-write! saw-bf2 (map midi->hz
                            (map (fn [midi-note] (+ 0 midi-note))
                                 (map note (take 256 (cycle score))))))

(ctl saw-s2 :freq-mul 1/40)
(kill ratatat)

(buffered-plain-space-organ :amp 0.8)
(kill buffered-plain-space-organ)

(stop)



(overtone.live/volume 0.03)



(use 'selfsyn.cosmoshelper)
(def reich-degrees [:vi :vii :i+ :_ :vii :_ :i+ :vii :vi :_ :vii :_])
(def pitches (degrees->pitches reich-degrees :diatonic :C4))
;;FIXME <---------------------------




(defonce mystical-aura-s (freesound-sample 166185))
(def m (mystical-aura-s))


;;BYE LOVERS AND FRIENDS
;(fadeout-master)
