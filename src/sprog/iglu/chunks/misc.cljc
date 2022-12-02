(ns sprog.iglu.chunks.misc
  (:require [clojure.walk :refer [postwalk 
                                  postwalk-replace]]))

(def trivial-vert-source
  '{:version "300 es"
    :precision {float lowp}
    :inputs {vertPos vec4}
    :main ((= gl_Position vertPos))})

(defn identity-frag-source [texture-type]
  (postwalk-replace
   (let [float-tex? (= texture-type :f8)]
     {:sampler-type (if float-tex? 'sampler2D 'usampler2D)
      :pixel-type (if float-tex? 'vec4 'uvec4)})
   '{:version "300 es"
     :precision {float highp
                 sampler2D highp
                 int highp
                 usampler2D highp}
     :uniforms {tex :sampler-type
                size vec2}
     :outputs {fragColor :pixel-type}
     :main ((= fragColor (texture tex (/ gl_FragCoord.xy size))))}))

(def rescale-chunk
  '{:functions
    {rescale
     {([float float float float float] float)
      ([oldMin oldMax newMin newMax x]
       (+ newMin
          (* (- newMax newMin)
             (/ (- x oldMin)
                (- oldMax oldMin)))))}}})

(def pos-chunk
  '{:functions {getPos {([] vec2)
                        ([]
                         (=float minDim (min size.x size.y))
                         (/ (- gl_FragCoord.xy
                               (* 0.5 (- size minDim)))
                            minDim))}}})

(def sympow-chunk
  '{:functions
    {sympow
     {([float float] float)
      ([x power]
       (* (sign x)
          (pow (abs x)
               power)))}}})

(def smoothstair-chunk
  '{:functions
    {smoothstair
     {([float float float] float)
      ([x steps steepness]
       (*= x steps)
       (=float c (- (/ 2 (- 1 steepness)) 1))
       (=float p (mod x 1))
       (/ (+ (floor x)
             (if (< p 0.5)
               (/ (pow p c)
                  (pow 0.5 (- c 1)))
               (- 1
                  (/ (pow (- 1 p) c)
                     (pow 0.5 (- c 1))))))
          steps))}}})

(def bilinear-usampler-chunk
  '{:functions
    {textureBilinear
     {([usampler2D vec2] vec4)
      ([tex pos]
       (=vec2 texSize (vec2 (textureSize tex i0)))
       (=vec2 texCoords (- (* pos texSize) 0.5))
       (=vec2 gridCoords (+ (floor texCoords) 0.5))
       (=vec2 tweenCoords (fract texCoords))
       (mix (mix (vec4 (texture tex (/ gridCoords texSize)))
                 (vec4 (texture tex (/ (+ gridCoords (vec2 1 0)) texSize)))
                 tweenCoords.x)
            (mix (vec4 (texture tex (/ (+ gridCoords (vec2 0 1)) texSize)))
                 (vec4 (texture tex (/ (+ gridCoords (vec2 1 1)) texSize)))
                 tweenCoords.x)
            tweenCoords.y))}}})

(def paretto-transform-chunk
  '{:functions {paretto
                {([float float float] float)
                 ([value shape scale]
                  (/ (pow (* shape scale) shape)
                     (pow value (+ shape 1))))}}})