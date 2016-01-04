(ns app.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [app.global :as g]
            cljsjs.d3
            [nw.gui :as ngui])

(:import goog.History))

(enable-console-print!)

(sec/set-config! :prefix "#")

;;setup history API
(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                      navigation
                      #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))

(defcomponent d3-view [app owner]
  (did-mount
   [_]
   (let [n 40
         random (js/d3.random.normal 0 0.2)
         data (-> (js/d3.range n)
                  (.map random))
         margin {:top 20 :right 20 :bottom 20 :left 40}
         width (- 960 (:left margin) (:right margin))
         height (- 500 (:top margin) (:bottom margin))
         x (-> (js/d3.scale.linear)
               (.domain #js [0 (- n 2)])
               (.range #js [0 width]))
         y (-> (js/d3.scale.linear)
               (.domain #js [-1 1])
               (.range #js [height 0]))
         line (-> (js/d3.svg.line)
                  (.interpolate "basis")
                  (.x (fn [d i]
                        (x i)))
                  (.y (fn [d i]
                        (y d))))
         _ (js/console.log "line:" line)
         svg (-> (js/d3.select "#chart-canvas")
                 (.append "svg")
                 (.attr "width"
                        (+ width (:left margin) (:right margin)))
                 (.attr "height"
                        (+ height (:bottom margin) (:top margin)))
                 (.append "g")
                 (.attr "transform"
                        (str "translate("
                             (:left margin)
                             ","
                             (:top margin)
                             ")")))
         _ (-> svg
               (.append "defs")
               (.append "clipPath")
               (.attr "id" "clip")
               (.append "rect")
               (.attr "width" width)
               (.attr "height" height))
         _ (-> svg
               (.append "g")
               (.attr "class" "x axis")
               (.attr "transform" (str "translate(0,"
                                       (y 0)
                                       ")"))
               (.call (-> (js/d3.svg.axis)
                          (.scale x)
                          (.orient "bottom"))))
         _ (-> svg
               (.append "g")
               (.attr "class" "y axis")
               (.call (-> (js/d3.svg.axis)
                          (.scale y)
                          (.orient "left"))))
         path (-> svg
               (.append "g")
               (.attr "clip-path" "url(#clip)")
               (.append "path")
               (.data data)
               (.attr "class" "line")
               (.attr "d" line))
         tick (fn tick []
                (.push data (random))
                (-> path
                    (.attr "d" line)
                    (.attr "transform" nil)
                    (.transition)
                    (.duration 500)
                    (.ease "linear")
                    (.attr "transform" (str "translate(" (x 0)
                                            ",0)"))
                    (.each "end" tick))
                (.shift data))]
     (tick)))
  
  (render
   [_]
   (html
    [:div#chart-canvas])))

(defcomponent hello [app owner]
  (render
   [_]
   (html
    (om/build d3-view app))))

(defn- init-menu []
  (let [submenu (ngui/create-menu)
        menu (ngui/create-menubar)
        item1 (ngui/create-menuitem #js {:label "item 1"})]
    
    (ngui/append-menuitem
     submenu
     item1
     (ngui/create-menuitem #js {:label "item 3"})
     (ngui/create-menuitem #js {:label "item 5"}))
    
    (ngui/append-menuitem
     menu
     (ngui/create-menuitem #js {:label "Main menu"
                                :submenu submenu}))

    (aset item1 "click" (fn []
                         (js/console.log "test menu click 2")))
    (ngui/set-menu-root menu)))

(defn main []
  (init-menu)
  (om/root hello
           g/app-state
           {:target (. js/document (getElementById "app"))}))
