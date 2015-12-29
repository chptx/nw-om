(ns utils.macro)

(defmacro this-call [jsobj method & args]
  (let [m (name method)]
    `(let [meth# (aget ~jsobj ~m)]
       (.call meth# ~jsobj ~@args))))
