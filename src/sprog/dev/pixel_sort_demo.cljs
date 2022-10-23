(ns sprog.dev.pixel-sort-demo
  (:require [sprog.util :as u]
            [sprog.webgl.canvas :refer [create-gl-canvas
                                        square-maximize-gl-canvas]]
            [sprog.webgl.shaders :refer [run-purefrag-shader!]]
            [sprog.webgl.textures :refer [create-f8-tex
                                          html-image-texture]]
            [sprog.input.mouse :refer [mouse-pos]]))

(def sort-resolution 1000)

(defonce gl-atom (atom nil))

(defonce frame-atom (atom nil))

(defonce texs-atom (atom nil))

; adapted from https://www.shadertoy.com/view/wsSczw
(def logic-frag-source
  '{:version "300 es"
    :precision {float highp}
    :uniforms {size vec2
               tex sampler2D
               frame int
               threshold float}
    :outputs {fragColor vec4}
    :signatures {main ([] void)
                 gscale ([vec3] float)}
    :functions {gscale
                ([c] (/ (+ c.r c.g c.b) "3.0"))
                main
                ([]
                 (=vec2 pos (/ gl_FragCoord.xy size))

                 (=float fParity (- (* (mod (float frame)
                                            "2.0")
                                       "2.0")
                                    "1.0"))
                 (=float vp (- (* (mod (floor (* pos.x size.x))
                                       "2.0")
                                  "2.0")
                               "1.0"))

                 (=vec2 dir (vec2 1 0))
                 (*= dir (* fParity vp))
                 (= dir (/ dir size))

                 (=vec4 curr (texture tex pos))
                 (=vec4 comp (texture tex (+ pos dir)))

                 (=float gCurr (gscale curr.rgb))
                 (=float gComp (gscale comp.rgb))

                 (= fragColor
                    (if (|| (< (+ pos.x dir.x) "0.0")
                            (> (+ pos.x dir.x) "1.0"))
                      (= fragColor curr)
                      (if (< dir.x "0.0")
                        (if (&& (> gCurr threshold)
                                (> gComp gCurr))
                          comp
                          curr)
                        (if (&& (> gComp threshold)
                                (> gCurr gComp))
                          comp
                          curr)))))}})

(defn update-page! []
  (let [gl @gl-atom
        resolution [gl.canvas.width gl.canvas.height]]
    (run-purefrag-shader! gl
                          logic-frag-source
                          sort-resolution
                          {:floats {"size" [sort-resolution sort-resolution]
                                    "threshold" (first (mouse-pos))}
                           :textures {"tex" (first @texs-atom)}
                           :ints {"frame" @frame-atom}}
                          {:target (second @texs-atom)})
    (swap! texs-atom reverse)


    (square-maximize-gl-canvas gl)
    (run-purefrag-shader! gl
                          '{:version "300 es"
                            :precision {float highp}
                            :uniforms {size vec2
                                       tex sampler2D}
                            :outputs {fragColor vec4}
                            :signatures {main ([] void)}
                            :functions {main
                                        ([]
                                         (=vec2 pos (/ gl_FragCoord.xy size))
                                         (= fragColor (texture tex pos)))}}
                          resolution
                          {:floats {"size" resolution}
                           :textures {"tex" (first @texs-atom)}})

    (swap! frame-atom inc))
  (js/requestAnimationFrame update-page!))

(defn init []
  (let [gl (create-gl-canvas)]
    (reset! gl-atom gl)
    (reset! texs-atom (u/gen 2 (create-f8-tex gl sort-resolution)))
    (reset! frame-atom 0)
    (run-purefrag-shader! gl
                          '{:version "300 es"
                            :precision {float highp}
                            :uniforms {size vec2
                                       tex sampler2D}
                            :outputs {fragColor vec4}
                            :signatures {main ([] void)}
                            :functions {main
                                        ([]
                                         (=vec2 pos (/ gl_FragCoord.xy size))
                                         (= pos.y (- "1.0" pos.y))
                                         (= fragColor (texture tex pos)))}}
                          sort-resolution
                          {:floats {"size" [sort-resolution sort-resolution]}
                           :textures {"tex"
                                      (html-image-texture gl "img")}}
                          {:target (first @texs-atom)}))
  (update-page!))
