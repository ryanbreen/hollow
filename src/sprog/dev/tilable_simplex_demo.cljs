(ns sprog.dev.tilable-simplex-demo
  (:require [sprog.util :as u]
            [sprog.webgl.canvas :refer [create-gl-canvas
                                        maximize-gl-canvas]]
            [sprog.webgl.shaders :refer [run-purefrag-autosprog!]]
            [sprog.iglu.chunks.noise :refer [tileable-simplex-2d-chunk]]
            [sprog.webgl.framebuffers :refer [target-screen!]]
            [sprog.iglu.core :refer [iglu->glsl]]
            [sprog.input.mouse :refer [mouse-pos]]))

(defonce gl-atom (atom nil))

(def frag-source
  (iglu->glsl
   nil
   tileable-simplex-2d-chunk
   '{:version "300 es"
     :precision {float highp}
     :uniforms {size vec2
                time float
                mouse vec2}
     :outputs {fragColor vec4}
     :signatures {main ([] void)}
     :functions {main
                 ([]
                  (=vec2 pos (/ gl_FragCoord.xy size))
                  (=float noiseValue
                          (* (+ (snoiseTileable2D (vec2 0)
                                                  (pow (vec2 "25.0") mouse)
                                                  (+ (* pos "3.0")
                                                     (vec2 100 -20)))
                                "1.0")
                             "0.5"))
                  (= fragColor (vec4 noiseValue
                                     noiseValue
                                     noiseValue
                                     1)))}}))

(defn update-page! []
  (let [gl @gl-atom
        width gl.canvas.width
        height gl.canvas.height
        resolution [width height]]
    (maximize-gl-canvas gl)
    (target-screen! gl)
    (run-purefrag-autosprog! gl
                        frag-source
                        resolution
                        {:floats {"size" resolution
                                  "mouse" (mouse-pos)}})
    (js/requestAnimationFrame update-page!)))

(defn init []
  (reset! gl-atom (create-gl-canvas))
  (update-page!))
