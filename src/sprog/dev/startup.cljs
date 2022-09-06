(ns sprog.dev.startup
  (:require sprog.dev.basic-demo
            sprog.dev.multi-texture-output-demo
            sprog.dev.fn-sort-demo
            sprog.dev.pixel-sort-demo))

(defn init []
  #_(sprog.dev.basic-demo/init)
  (sprog.dev.multi-texture-output-demo/init)
  #_(sprog.dev.fn-sort-demo/init)
  #_(sprog.dev.pixel-sort-demo/init))

(defn pre-init []
  (js/window.addEventListener "load" (fn [_] (init))))
