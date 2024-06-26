(ns hollow.demos.nonconvex-polygon
  (:require [hollow.util :as u]
            [hollow.dom.canvas :refer [maximize-gl-canvas
                                       canvas-resolution]]
            [hollow.webgl.shaders :refer [run-shaders!]]
            [hollow.webgl.core :refer [clear!
                                       enable!
                                       set-stencil-func!
                                       set-stencil-op!
                                       set-color-mask!]]
            [hollow.webgl.attributes :refer [create-boj!]]
            [hollow.webgl.core
             :refer-macros [with-context]
             :refer [stop-hollow!
                     start-hollow!]]))

(def shape
  [[0.5 -0.5]
   [0.5 0.5]
   [-0.5 0.5]
   [-0.5 -0.5]
   [0 0]])

(defn render! [gl]
  (with-context gl
    (maximize-gl-canvas {:aspect-ratio 1})
    (letfn [(draw-triangles!
              [color triangles]
              (run-shaders!
               ['{:precision {float highp}
                  :inputs {position vec2}
                  :main
                  ((= gl_Position
                      (vec4 position
                            0
                            1)))}
                '{:precision {float highp}
                  :uniforms {color vec3}
                  :outputs {frag-color vec4}
                  :main
                  ((= frag-color (vec4 color 1)))}]
               (canvas-resolution)
               {"color" color}
               {"position"
                (create-boj! 2 {:initial-data
                                (js/Float32Array. (apply concat triangles))})}
               0
               (* 3 (count triangles))))]
      (clear! :all)
      (enable! :stencil-test)
      (set-color-mask! false)
      (set-stencil-func! :always 0 1)
      (set-stencil-op! :keep :keep :invert)
      (draw-triangles! [0 0 0] (map (fn [[a b]]
                                      (concat a b (first shape)))
                                    (partition 2 1 (rest shape))))

      (set-color-mask! true)
      (set-stencil-func! :equal 1 1)
      (set-stencil-op! :keep)
      (let [[[min-x max-x] [min-y max-y]]
            (mapv (fn [accessor]
                    (mapv (fn [selector]
                            (apply selector (map accessor shape)))
                          [min max]))
                  [first second])]
        (draw-triangles! [1 0 0]
                         [[min-x min-y
                           max-x max-y
                           max-x min-y]
                          [min-x min-y
                           max-x max-y
                           min-x max-y]])))
    {}))

(defn init []
  (start-hollow! render!
                 nil
                 {:stencil? true}))
