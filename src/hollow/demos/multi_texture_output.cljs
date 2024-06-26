(ns hollow.demos.multi-texture-output
  (:require [hollow.util :as u]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            [hollow.webgl.textures :refer [create-tex]]
            [kudzu.core :refer [kudzu->glsl]]
            [hollow.webgl.core :refer-macros [with-context]
             :refer [start-hollow!]]))

(def texture-resolution 8)

(def render-frag-source
  (kudzu->glsl
   {:constants {:texture-resolution texture-resolution}}
   '{:precision {float highp}
     :outputs {frag-color0 vec4
               frag-color1 vec4}
     :layout {frag-color0 0
              frag-color1 1}
     :main ((=vec2 pos (/ gl_FragCoord.xy :texture-resolution))
            (= frag-color0 (vec4 pos
                                0
                                1))
            (= frag-color1 (vec4 0
                                pos
                                1)))}))

(def draw-frag-source
  (kudzu->glsl
   '{:precision {float highp
                 sampler2D highp}
     :uniforms {size vec2
                tex1 sampler2D
                tex2 sampler2D}
     :outputs {frag-color vec4}
     :main ((=vec2 pos (/ gl_FragCoord.xy size))
            (= frag-color
               (if (< pos.x 0.5)
                 (texture tex1 (* pos (vec2 2 1)))
                 (texture tex2 (* (- pos (vec2 0.5 0))
                                  (vec2 2 1))))))}))

(defn update-page! [{:keys [gl tex1 tex2] :as state}]
  (with-context gl
    (maximize-gl-canvas)
    (run-purefrag-shader! draw-frag-source
                          (canvas-resolution)
                          {"size" (canvas-resolution)
                           "tex1" tex1
                           "tex2" tex2}))
  state)

(defn init-page! [gl]
  (with-context gl
    (let [[tex1 tex2]
          (u/gen 2 (create-tex :f8
                               texture-resolution
                               {:filter-mode :nearest}))]
      (run-purefrag-shader! render-frag-source
                            texture-resolution
                            {}
                            {:target [tex1 tex2]})
      {:tex1 tex1
       :tex2 tex2})))

(defn init []
  (start-hollow! init-page!
                 update-page!))
