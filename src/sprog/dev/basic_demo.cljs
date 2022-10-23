(ns sprog.dev.basic-demo
  (:require [sprog.util :as u]
            [sprog.webgl.canvas :refer [create-gl-canvas
                                        maximize-gl-canvas]]
            [sprog.webgl.shaders :refer [run-purefrag-autosprog!]]
            [sprog.webgl.framebuffers :refer [target-screen!]]))

(defonce gl-atom (atom nil))

(defn update-page! []
  (let [gl @gl-atom
        resolution [gl.canvas.width gl.canvas.height]]
    (maximize-gl-canvas gl)
    (target-screen! gl)
    (run-purefrag-autosprog! gl
                             '{:version "300 es"
                               :precision {float highp}
                               :uniforms {size vec2}
                               :outputs {fragColor vec4}
                               :signatures {main ([] void)}
                               :functions {main
                                           ([]
                                            (=vec2 pos (/ gl_FragCoord.xy size))
                                            (= fragColor (vec4 pos 0 1)))}}
                             resolution
                             {:floats {"size" resolution}})
    (js/requestAnimationFrame update-page!)))

(defn init []
  (reset! gl-atom (create-gl-canvas))
  (update-page!))
