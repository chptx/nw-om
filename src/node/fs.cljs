(ns node.fs
  (:require-macros [utils.macro :as jsm]))

(def fs (js/require "fs"))

(defn create-readstream [path]
  (.createReadStream fs "/etc/hosts"))

(defn open-file-ro [path cb]
  (.open fs path "r" cb))

(defn read [fd buffer pos len cb]
  (.read fs fd buffer 0 len pos cb))
