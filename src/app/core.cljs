(ns app.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as sec :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [app.global :as g]
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

(defcomponent hello [app owner]
  (render
   [_]
   (html [:div "hello nw & om"])))

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
                         (js/console.log "test menu click")))
    (ngui/set-menu-root menu)))

(defn main []
  (init-menu)
  (om/root hello
           g/app-state
           {:target (. js/document (getElementById "app"))}))
