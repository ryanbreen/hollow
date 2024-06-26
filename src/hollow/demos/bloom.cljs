(ns hollow.demos.bloom
  (:require [hollow.util :as u]
            [hollow.webgl.textures :refer [html-image-tex]]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            (hollow.input.mouse :refer [mouse-pos])
            [kudzu.chunks.postprocessing :refer [get-bloom-chunk
                                                 square-neighborhood]]
            [kudzu.core :refer [kudzu->glsl]]
            [hollow.webgl.core
             :refer-macros [with-context]
             :refer [start-hollow!]]))

(def frag-source
  (kudzu->glsl
   (get-bloom-chunk :f8 (square-neighborhood 4 1) 5)
   '{:precision {float highp}
     :uniforms {size vec2
                mouse vec2
                tex sampler2D}
     :outputs {frag-color vec4}
     :main ((=vec2 pos (/ gl_FragCoord.xy size))
            (= pos.y (- 1 pos.y))
            (= frag-color (-> (bloom tex
                                    pos
                                    (* (bi->uni mouse.x) 0.0025)
                                    (bi->uni mouse.y))
                             .xyz
                             (vec4 1))))}))

(defn update-page! [{:keys [gl texture] :as state}]
  (with-context gl
    (maximize-gl-canvas {:aspect-ratio 1})
    (run-purefrag-shader! frag-source
                          (canvas-resolution)
                          {"size" (canvas-resolution)
                           "mouse" (mouse-pos)
                           "tex" texture}))
  state)

(defn init-page! [gl]
  {:texture (html-image-tex gl "img")})

(defn init []
  (start-hollow! init-page! update-page!))
