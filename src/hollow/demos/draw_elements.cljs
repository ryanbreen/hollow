(ns hollow.demos.draw-elements
  (:require [hollow.util :as u]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-shaders!]]
            [hollow.webgl.attributes :refer [create-boj!]]
            [hollow.webgl.core
             :refer-macros [with-context]
             :refer [start-hollow!
                     hollow-context]]))

(def pos-buffer-data [0 0
                      0.5 0
                      0 0.5
                      0.5 0.5])

(def color-buffer-data [1 0 0
                        0 1 0
                        0 0 1
                        1 0 0])

(def index-buffer-data [0 1 2
                        3 1 2])

(def vert-source
  '{:precision {float highp}
    :inputs {vertexPos vec2
             vertexColor vec3}
    :outputs {color vec3}
    :uniforms {rotation mat2}
    :main ((= color vertexColor)
           (= gl_Position (vec4 (* vertexPos rotation) 0 1)))})

(def frag-source
  '{:precision {float highp}
    :inputs {color vec3}
    :outputs {frag-color vec4}
    :main ((= frag-color (vec4 color 1)))})

(defn update-page! [{:keys [gl pos-boj color-boj index-boj] :as state}]
  (with-context gl
    (maximize-gl-canvas {:aspect-ratio 1})
    (run-shaders! [vert-source frag-source]
                  (canvas-resolution)
                  {"rotation"
                   (let [angle (u/seconds-since-startup)]
                     [(Math/cos angle) (- (Math/sin angle))
                      (Math/sin angle) (Math/cos angle)])}
                  {"vertexPos" pos-boj
                   "vertexColor" color-boj}
                  0
                  6
                  {:indices index-boj}))
  state)

(defn init-page! [gl]
  (with-context gl
    {:pos-boj
     (create-boj! 2
                  {:initial-data (js/Float32Array. pos-buffer-data)})
     :color-boj
     (create-boj! 3
                  {:initial-data (js/Float32Array. color-buffer-data)})
     :index-boj
     (create-boj! 1
                  {:initial-data (js/Uint16Array. index-buffer-data)
                   :type :u16
                   :elements? true})}))

(defn init []
  (start-hollow! init-page! update-page!))
