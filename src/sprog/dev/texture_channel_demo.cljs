(ns sprog.dev.texture-channel-demo
  (:require [sprog.util :as u]
            [clojure.walk :refer [postwalk-replace]]
            [sprog.webgl.canvas :refer [create-gl-canvas
                                        maximize-gl-canvas]]
            [sprog.webgl.shaders :refer [run-purefrag-autosprog]]
            [sprog.webgl.textures :refer [create-f8-tex]]))

(def texture-resolution 8)

(defonce gl-atom (atom nil))

(defonce texture-atom (atom nil))

(def render-frag-source
  (postwalk-replace
   {:texture-resolution-f (.toFixed texture-resolution 1)}
   '{:version "300 es"
     :precision {float highp}
     :outputs {fragColor vec2}
     :signatures {main ([] void)}
     :functions {main
                 ([]
                  (=vec2 pos (/ gl_FragCoord.xy :texture-resolution-f))
                  (= fragColor (vec2 pos)))}}))

(def draw-frag-source
  '{:version "300 es"
    :precision {float highp
                sampler2D highp}
    :uniforms {size vec2
               tex sampler2D}
    :outputs {fragColor vec4}
    :signatures {main ([] void)}
    :functions {main
                ([]
                 (=vec2 pos (/ gl_FragCoord.xy size))
                 (=vec4 textureColor (texture tex pos))
                 (= fragColor (vec4 textureColor.xy 0 1)))}})

(defn update-page! []
  (let [gl @gl-atom
        resolution [gl.canvas.width gl.canvas.height]]
    (maximize-gl-canvas gl)
    (run-purefrag-autosprog gl
                            draw-frag-source
                            resolution
                            {:floats {"size" resolution}
                             :textures {"tex" @texture-atom}})
    (js/requestAnimationFrame update-page!)))

(defn init []
  (let [gl (create-gl-canvas)]
    (reset! gl-atom gl)
    (reset! texture-atom (create-f8-tex gl
                                        texture-resolution
                                        {:filter-mode :nearest
                                         :channels 2}))
    (run-purefrag-autosprog gl
                            render-frag-source
                            texture-resolution
                            {}
                            {:target @texture-atom}))
  (update-page!))
