(ns hollow.demos.texture-3d
  (:require [hollow.util :as u]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-purefrag-shader!]]
            [hollow.webgl.textures :refer [create-tex]]
            [kudzu.core :refer [kudzu->glsl]]
            [hollow.webgl.core
             :refer-macros [with-context]
             :refer [start-hollow!]]))

(def tex-size 6)

(defn update-page! [{:keys [gl texture] :as state}]
  (with-context gl
    (maximize-gl-canvas {:aspect-ratio 1})
    (run-purefrag-shader! '{:precision {float highp
                                        sampler3D highp}
                            :uniforms {size vec2
                                       time float
                                       tex sampler3D}
                            :outputs {frag-color vec4}
                            :main ((=vec2 pos (/ gl_FragCoord.xy size))
                                   (= frag-color
                                      (texture tex
                                               (vec3 pos (mod time 1)))))}
                          (canvas-resolution)
                          {"size" (canvas-resolution)
                           "time" (u/seconds-since-startup)
                           "tex" texture}))
  state)

(defn init-page! [gl]
  (with-context gl
    (let [tex (create-tex :f8
                          [tex-size tex-size 4]
                          {:wrap-mode [:clamp :clamp :repeat]})]
      (run-purefrag-shader!
       (kudzu->glsl
        {:constants {:tex-size-f (.toFixed tex-size 1)}}
        '{:precision {float highp
                      sampler3D highp}
          :outputs {layer1Color vec4
                    layer2Color vec4
                    layer3Color vec4
                    layer4Color vec4}
          :layout {layer1Color 0
                   layer2Color 1
                   layer3Color 2
                   layer4Color 3}
          :main
          ((=float colorValue
                   (/ (length
                       (/ gl_FragCoord.xy :tex-size-f))
                      (sqrt 2)))
           (= layer1Color (vec4 colorValue 0 0 1))
           (= layer2Color (vec4 0 colorValue 0 1))
           (= layer3Color (vec4 0 0 colorValue 1))
           (= layer4Color
              (vec4 colorValue colorValue colorValue 1)))})
       tex-size
       {}
       {:target [[tex 0] [tex 1] [tex 2] [tex 3]]})
      {:texture tex})))

(defn init []
  (start-hollow! init-page! update-page!))
