(ns selfsyn.cosmoshelper
    (:use [overtone.live]
          [mud.core]
          [selfsyn.wavs]
          )
    (:require [mud.timing :as time][overtone.studio.fx :as fx][shadertone.tone :as t])
    )


(definst plucked [note-buf 0 beat-bus (:count time/beat-1th) beat-trg-bus (:beat time/beat-4th) note_slide 0 note_slide_shape 5 note_slide_curve 0 amp 1 amp_slide 0 amp_slide_shape 5
                    amp_slide_curve 0 pan 0 pan_slide 0 pan_slide_shape 5 pan_slide_curve 0 attack 0 decay 0 sustain 0
                    release 0.2 attack_level 1 sustain_level 1 env_curve 2 out_bus 0 cutoff 100 cutoff_slide 0
                    cutoff_slide_shape 5 cutoff_slide_curve 0 blip_rate 0.5 decay_time 0.9 decay_coef 0.4 room 200
                    reverb 8]
    (let [cnt       (in:kr beat-bus)
          trg       (in:kr beat-trg-bus)
          note      (buf-rd:kr 1 note-buf cnt)
          gate-trg (and (> note 0) trg)
          vol (set-reset-ff gate-trg)
          note      (varlag note note_slide note_slide_curve note_slide_shape)
          amp       (varlag amp amp_slide amp_slide_curve amp_slide_shape)
          pan       (varlag pan pan_slide pan_slide_curve pan_slide_shape)
          cutoff    (varlag cutoff cutoff_slide cutoff_slide_curve cutoff_slide_shape)
          cutoff-freq (midicps cutoff)
          freq (midicps note)
          amp-fudge 1
          src (sum [(sin-osc freq)
                    (saw freq)
                    (blip freq (* blip_rate (sin-osc:kr blip_rate)))])
          dly  (/ 1 freq)
          src (pluck src gate-trg dly dly decay_time (min decay_coef 0.99))
          src (rlpf src 1000)
          src (g-verb src (max room,1) reverb)
          src (lpf src cutoff-freq)
          env (env-gen:kr (env-adsr-ng attack decay sustain release attack_level sustain_level) :gate gate-trg)]
      (pan2 (* vol amp-fudge env src) pan amp)))

(definst bass [note-buf 0 beat-bus (:count time/beat-1th) beat-trg-bus (:beat time/beat-4th) note_slide 0 note_slide_shape 5 note_slide_curve 0 amp 1 amp_slide 0 amp_slide_shape 5
                    amp_slide_curve 0 pan 0 pan_slide 0 pan_slide_shape 5 pan_slide_curve 0 attack 0 decay 0 sustain 0
                    release 0.2 attack_level 1 sustain_level 1 env_curve 2 out_bus 0 cutoff 100 cutoff_slide 0
                    cutoff_slide_shape 5 cutoff_slide_curve 0 blip_rate 0.5 decay_time 0.9 decay_coef 0.4 room 200
                    reverb 8]
    (let [cnt       (in:kr beat-bus)
          trg       (in:kr beat-trg-bus)
          note      (buf-rd:kr 1 note-buf cnt)
          gate-trg (and (> note 0) trg)
          vol (set-reset-ff gate-trg)
          note      (varlag note note_slide note_slide_curve note_slide_shape)
          amp       (varlag amp amp_slide amp_slide_curve amp_slide_shape)
          pan       (varlag pan pan_slide pan_slide_curve pan_slide_shape)
          cutoff    (varlag cutoff cutoff_slide cutoff_slide_curve cutoff_slide_shape)
          cutoff-freq (midicps cutoff)
          freq (midicps note)
          amp-fudge 1
          src (sum [(sin-osc freq)
                    (lf-saw freq)
                    (blip freq (* blip_rate (sin-osc:kr blip_rate)))])
          dly  (/ 1 freq)
          src (rlpf src 2000)
          src (g-verb src (max room,1) reverb)
          src (lpf src 200)
          env (env-gen:kr (env-adsr-ng attack decay sustain release attack_level sustain_level) :gate gate-trg)]
      (pan2 (* vol amp-fudge env src) pan amp)))

(definst plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1]
  (let [freq   (midicps note)
        noize  (* 0.8 (white-noise))
        dly    (/ 1.0 freq)
        plk    (pluck noize gate dly dly decay coef)
        dist   (distort plk)
        filt   (rlpf dist (* 12 freq) 0.6)
        clp    (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur)) reverb)))

(definst beep [note 60]
  (let [sound-src (sin-osc (midicps note))
        env       (env-gen (perc 0.01 1.0) :action FREE)] ; sam uses :free
    (* sound-src env)))


(defonce __sample_cache__ (atom {}))
(def SAMPLE-ROOT "resources/samples/")

(defn load-local-sample     [sample] (load-sample (str SAMPLE-ROOT sample)))
(defn local-recording-start [name]   (recording-start (str SAMPLE-ROOT name)))

(defonce directory-for-samples (clojure.java.io/file "resources/samples/"))
(defonce all-files-samples (file-seq directory-for-samples))
(defonce ether-set (file-seq (clojure.java.io/file "/Users/josephwilk/Workspace/music/samples/Ether")))
(defonce mountain-set (file-seq (clojure.java.io/file "/Users/josephwilk/Workspace/music/samples/Mountain")))

(defn find-sample
([match idx] (find-sample match idx all-files-samples))
([match idx sample-files]
     (let [sample-key (str match ":" idx)]
       (or (get sample-key @__sample_cache__)
           (let [r  (->> (filter #(and
                                   (re-find #"\.wav" (.getName %))
                                   (re-find (re-pattern (str "(?i)" match)) (.getName %))) sample-files)
                         (map #(.getAbsolutePath %1)))
                 sample (sample  (nth r (mod idx (count r))))]
             (swap! __sample_cache__ assoc sample-key sample)
             sample)))))

(defn repl-player [sample & rargs]
  (let [args (apply hash-map rargs)]
    (if (and (seq args) (< (:rate args) 0))
      (sample-player sample  :rate (:rate args) :start-pos (/ (:size sample) 2))
      (apply sample-player sample rargs))))
