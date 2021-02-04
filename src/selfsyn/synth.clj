(ns selfsyn.synth
  (:use [overtone.live]
        [clojure.java.shell :only [sh]])
  (:require [mud.timing :as time]))

(defn write-file[text path]
  (with-open [w (clojure.java.io/writer  path)]
    (.write w (str text))))

(defn say
  [text lang]
  (let [uuid  (subs (str (overtone.helpers.lib/uuid)) 0 5)
        in-file  (str "resources/audio/" uuid ".txt")
        out-file (str "resources/audio/" uuid ".wav")]
    (do
      (write-file text in-file)
      (sh "espeak" "-v" lang  "-f" in-file "-w" out-file ))
    out-file))

(defn voz-buffer [txt]
  (let [file (say txt "es-la")]
    (sample file)))

(comment
  (def m1 ((voz-buffer
            "yo soy verdadero, pero mi verdad transciende este universo"
            ) :rate 0.95 :loop? true :out-bus 0))
  (def m2 ((voz-buffer "que los cumplas feliz que los cumplas") :rate 1. :loop? true :out-bus 0))
  (def m3 ((voz-buffer  "que los cumplas Ivon") :rate 0.95 :loop? true :out-bus 0))
  (def m4 ((voz-buffer  "que los cumplas feliz") :rate 0.95 :loop? true :out-bus 0))

  (ctl m1 :rate 0.2 :wob-hi 200 :volume 0.5 :note 20)
  (ctl m1 :rate 0.85 :amp 0.8)
  (ctl m1 :rate 0.5)
  (ctl m2 :rate 0.5)
  (ctl m4 :rate 1. :note 150)
  (kill m1)
  (kill m2)
  (kill m3)
  (stop))

(defsynth monotron
  "Korg Monotron from website diagram:
   http://korg.com/services/products/monotron/monotron_Block_diagram.jpg."
  [note     60            ; midi note value
   volume   0.7           ; gain of the output
   mod_pitch_not_cutoff 1 ; use 0 or 1 only to select LFO pitch or cutoff modification
   pitch    0.0           ; frequency of the VCO
   rate     4.0           ; frequency of the LFO
   int      1.0           ; intensity of the LFO
   cutoff   1000.0        ; cutoff frequency of the VCF
   peak     0.5           ; VCF peak control (resonance)
   pan      0             ; stereo panning
   ]
  (let [note_freq       (midicps note)
        pitch_mod_coef  mod_pitch_not_cutoff
        cutoff_mod_coef (- 1 mod_pitch_not_cutoff)
        LFO             (* int (saw rate))
        VCO             (saw (+ note_freq pitch (* pitch_mod_coef LFO)))
        vcf_freq        (+ cutoff (* cutoff_mod_coef LFO) note_freq)
        VCF             (moog-ff VCO vcf_freq peak)
        ]
    (out 0 (pan2 (* volume VCF) pan))))

(comment
  (def N0 (monotron 40 0.8 1 0.0 2.5 350.0 800.0 3.0))
  (ctl N0 :note   40)               ;; midi note value: 0 to 127
  (ctl N0 :volume 0.7)              ;; gain of the output: 0.0 to 1.0
  (ctl N0 :mod_pitch_not_cutoff 0)  ;; use 0 or 1 only to select LFO pitch or cutoff modification
  (ctl N0 :pitch  0.10)             ;; this + note is frequency of the VCO

  (ctl N0 :rate   .15)               ;; frequency of the LFO
  (ctl N0 :int    800.0)           ;; intensity of the LFO
  (ctl N0 :cutoff 600.0)           ;; cutoff frequency of the VCF
  (ctl N0 :peak   0.5)              ;; VCF peak control (resonance) 0.0 to 4.0
  (kill N0)
  (stop)
  (show-graphviz-synth monotron)
  )

(defsynth distorted-feedback []
  (let [noiz (mul-add (lf-noise0:kr 0.5) 2 2.05)
        input (* 0.15 (crackle  1.5))
        fb-in (local-in 1)
        snd (+ input (leak-dc (* 1.1 (delay-n fb-in 3.5 noiz))))
        snd (rlpf snd (mul-add (lf-noise0:kr noiz) 400 800) 0.5)
        snd (clip:ar snd 0 0.9)
        fb-out (local-out snd)]
    (out 0 (pan2 snd))))

(comment
  (def df (distorted-feedback))
  (kill df))

(defsynth floaty
  [note 50 t 4 amt 0.3 amp 0.8 dur 3 lpf-cutoff 1000 lpf-peak 0.5 out-bus 0]
  (let [freq       (midicps note)
        f-env      (env-gen (perc t t) 1 1 0 1 FREE)
        src        (saw [freq (* freq 1.01)])
        signal     (rlpf (* 0.3 src)
                         (+ (* 0.6 freq) (* f-env 2 freq)) 0.2)
        k          (/ (* 2 amt) (- 1 amt))
        distort    (/ (* (+ 1 k) signal) (+ 1 (* k (abs signal))))
        gate       (pulse (* 2 (+ 1 (sin-osc:kr 0.05))))
        compressor (compander distort gate 0.01 1 0.5 0.01 0.01)
        dampener   (+ 1 (* 0.5 (sin-osc:kr 0.5)))
        reverb     (free-verb compressor 0.5 0.5 dampener)
        echo       (comb-n reverb 0.4 0.3 0.5)]
    (line 0 1 dur :action FREE)
    (out out-bus (pan2 (* amp reverb)))))

(definst grumble [speed 6 freq-mul 1]
  (let [snd (mix (map #(* (sin-osc (* % freq-mul 100))
                          (max 0 (+ (lf-noise1:kr speed)
                                    (line:kr 1 -1 30 :action FREE))))
                      [1 (/ 2 3) (/ 3 2) 2]))]
    (pan2 snd (sin-osc:kr 50))))

(comment
  (def g (grumble :freq-mul 1))
  (ctl g :sp)
  (ctl grumble :speed 130)
  (volume (/  127  127))
  )

(defsynth buffer->tap [beat-buf 0 beat-bus 0 beat-size 16 measure 6]
  "Exposes some useful timing information which we can use in Shadertone"
  (let [cnt (in:kr beat-bus)
        beat (buf-rd:kr 1 beat-buf cnt)
        _  (tap "beat"          60 (a2k beat))
        _  (tap "beat-count"    60 (a2k (mod cnt beat-size)))
        _  (tap "measure-count"       60 (a2k (/ (mod cnt (* measure beat-size)) measure)))
        _  (tap "beat-total-count" 60 (a2k (mod cnt (* measure beat-size))))])
  (out 0 0))

(defsynth seqer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8
   beat-bus (:count time/main-beat)     ;; Our beat count
   beat-trg-bus (:beat time/main-beat)  ;; Our beat trigger
   amp 0.7
   rate-start 0.1
   rate-limit 0.9]
  (let [cnt      (in:kr beat-bus)
        rander (mod cnt 1)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol amp (scaled-play-buf :num-channels 1 :buf-num buf :rate (t-rand:kr rate-start rate-limit rander) :trigger bar-trg)))))
