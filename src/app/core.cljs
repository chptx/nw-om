(ns app.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [app.global :as g]
            #_cljsjs.d3
            cljsjs.dygraph
            cljsjs.long
            cljsjs.bytebuffer
            [om-bootstrap.button :as b]
            [om-bootstrap.nav :as n]
            [om-bootstrap.random :as r]
            [node.fs :as fs]
            [nw.gui :as ngui])

(:import goog.History))

(enable-console-print!)

(sec/set-config! :prefix "#")

(defn- create-dygraph [container data labels]
  (js/Dygraph. container data (clj->js {:drawPoints false
                                       :showRoller false
                                        ;:series {"b" {:axis "y2"}}
                                        :drawAxesAtZero true
                                        ;:width "100%"
                                        :axes
                                        {
                                         :y {
                                             :drawAxis false
                                             }
                                        
                                         }
                                       :labels labels})))
(defn- update-data [data unit]
  (.push data unit)
  (.shift data))

(defn- init-data []
  (let [t (.getTime (js/Date.))]
    (clj->js
     (map
      (fn [i] [(js/Date. (- t (* i 1000))) (js/Math.random) (js/Math.random)])
      (rseq (vec (range 100)))))))

;;setup history API
(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                      navigation
                      #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))


#_(defcomponent d3-view [app owner]
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
               (.domain #js [1 (- n 2)])
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
               (.data #js [data])
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

(defn- reset-chart []
  (let [c
        (js/document.querySelector "#chart-canvas")]
    (create-dygraph c (init-data) ["a" "b" "c"])))

(defcomponent dygraph-view [app owner]
  (did-mount
   [_]
   #_(ngui/set-resizable false)
   (reset-chart))
  (render
   [_]
   (html

    [:div#chart-canvas
     {:style {:width "100%"
              :minHeight "640px"}}
     ])))



(defn- choose-file [name cb]
  (let [chooser (js/document.querySelector name)]
    (.addEventListener
     chooser
     "change"
     (fn [evt]
       (this-as this
                (js/console.log "evt" (aget this "value"))
                (when-let [fname (aget this "value")]
                  (cb fname))))
     false)
    (.click chooser)))


(defn- process-buffer [err cnt buf]
  (let [bs (js/Uint8Array. buf)
        bb (.append (js/ByteBuffer.) bs)]
    (js/console.log "bb:" (.readByte bb 0))))

(defn- process-file [err fd]
  (let [buf (js/Buffer. 32)]
    (fs/read fd buf 1 10 process-buffer)))

(defn- init-menu []
  (let [submenu (ngui/create-menu)
        menu (ngui/create-menubar)
        load-file (ngui/create-menuitem #js {:label "载入.."})
        window-exit (ngui/create-menuitem #js {:label "退出"})]
    
    (ngui/append-menuitem
     submenu
     load-file
     window-exit)
    
    (ngui/append-menuitem
     menu
     (ngui/create-menuitem #js {:label "文件"
                                :submenu submenu}))
    (aset window-exit "click" (fn []
                                (ngui/exit-app)))
    (aset load-file "click" (fn []
                              (choose-file "#fileDialog" (fn [f] (fs/open-file-ro f process-buffer)))))
    (ngui/set-menu-root menu)))


(defcomponent hello [app owner]
  (render
   [_]
   (html
    [:div
     (n/navbar {:brand "test"}
               (b/button {:class "navbar-right"
                          :bs-style "danger"
                          :onClick (fn []
                                     (ngui/close-window))}
                         (r/glyphicon {:glyph "remove"}))
               (n/nav {}
                      (b/dropdown {:title "文件"}
                                  (b/menu-item {:key 1
                                                :onClick
                                                (fn []
                                                  (choose-file "#fileDialog" (fn [f] (fs/open-file-ro f process-buffer))))} "载入..")
                                  (b/menu-item {:divider? true})
                                  (b/menu-item {:key 2
                                                :onClick
                                                (fn []
                                                  (ngui/close-window))} "退出")
                                  )
               )
               (b/button {:bs-style "primary"}
                           "test")

                )
     (om/build dygraph-view app)])))

(defn main []
  (ngui/maximize)
;  (fs/open-file-ro "/etc/hosts" process-file)
  #_(init-menu)
  (om/root hello
           g/app-state
           {:target (. js/document (getElementById "app"))}))
