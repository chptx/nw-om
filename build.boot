(set-env!
 :source-paths   #{"src"}
 :resource-paths #{"html"}
 :dependencies '[
                 [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                 [adzerk/boot-cljs          "1.7.170-3"      :scope "test"]
                 [com.cemerick/piggieback "0.2.1" :scope "test"]
                 [weasel "0.7.0" :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.0" :scope "test"]
                 [adzerk/boot-reload        "0.4.2"           :scope "test"]
                 [pandeiro/boot-http        "0.7.0"  :scope "test"]
                 [net.unit8/tower-cljs "0.1.0" :scope "test"]
                 [boot-deps "0.1.6" :scope "test"]
                 [org.omcljs/om "0.9.0" :exclusions [cljsjs/react]]
                 [cljsjs/react "0.14.3-0"]
                 [prismatic/om-tools "0.4.0"]
                 [secretary "1.2.3"]
                 [sablono "0.4.0" :exclusions [cljsjs/react]]
		 [cljsjs/d3 "3.5.7-1"]
                 [cljsjs/dygraph "1.1.1-0"]
                 [org.clojure/clojure       "1.7.0"]
                 [org.clojure/clojurescript "1.7.189"]])

(require
  '[adzerk.boot-cljs      :refer [cljs]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload    :refer [reload]]
  '[pandeiro.boot-http    :refer [serve]])

(deftask dev []
  (set-env! :source-paths #{"src" "test"})
  (comp (serve :dir "target/" :httpkit true)
        (watch)
        (speak)
        (reload :on-jsload 'app.core/main)
        (cljs-repl)
        (cljs :source-map true :optimizations :none)))

(deftask build []
  (set-env! :source-paths #{"src"})
  (comp (cljs :optimizations :advanced)))
