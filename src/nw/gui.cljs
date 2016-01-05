(ns nw.gui
  (:require-macros [utils.macro :as jsm]))

(def gui (js/require "nw.gui"))

(defn create-menu [& [opt]]
  (let [menu-class (aget gui "Menu")]
    (if opt
      (menu-class. opt)
      (menu-class.))))

(defn create-menuitem [opt]
  (let [menuitem-class (aget gui "MenuItem")]
    (menuitem-class. opt)))

(defn create-menubar []
  (create-menu #js {:type "menubar"}))

(defn append-menuitem [menu & items]
  (doseq [i items]
    (jsm/this-call menu append i))
  menu)

(defn set-menu-root [menu]
  (let [w (aget gui "Window")
        iw (jsm/this-call w get)]
    (aset iw "menu" menu)))

(defn maximize []
  (let [w (aget gui "Window")
        iw (jsm/this-call w get)]
    (jsm/this-call iw maximize)))

(defn set-resizable [s]
  (let [w (aget gui "Window")
        iw (jsm/this-call w get)]
    (jsm/this-call iw setResizable s)))

(defn close-window []
  (let
    [w (aget gui "Window")
     iw (jsm/this-call w get)]
    (jsm/this-call iw close)))

(defn exit-app []
  (let [w (aget gui "App")]
    (jsm/this-call w quit)))
