(ns scicloj.tableplot.v1.dag
  (:require [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [scicloj.tableplot.v1.cache :as cache]
            [scicloj.tableplot.v1.xform :as xform]))

(defn xform-k
  "Apply Hanami xform
  to a fetch specific key
  given a substitution map.

  For example:
  (xform-k :B
         {:A 9
          :B (fn [{:keys [A]}] (inc A))
          :C (fn [{:keys [B]}] (inc B))})
  => 10
  "
  [k submap]
  (-> {:result k}
      (xform/xform submap)
      :result))

(defn cached-xform-k
  "Apply Hanami xform
  to fetch a specific key
  given a substitution map,
  using the cache.

  For example:

  ```clj
  (let [verbose-inc (fn [{:keys [A]}]
                       (prn :computing)
                       (inc A))]
    (cache/with-clean-cache
      (dotimes [i 2]
        (prn
         (cached-xform-k :B
                         {:A 9
                          :B verbose-inc})))))
  ;; printed output:

  :computing
  10
  10
  ```
  "
  [k submap]
  (let [id [k submap]]
    (if-let [result (@cache/*cache id)]
      result
      (let [computed-result (xform-k k submap)]
        (swap! cache/*cache
               assoc id computed-result)
        computed-result))))

(defn fn-with-deps-keys
  "Given a set of dependency keys and a submap function,
  create a submap function that first makes sure
  that the xform results for these keys are available.

  For example:

  ```clj
  (cache/with-clean-cache
    (-> {:b :B
         :c :C
         ::ht/defaults {:B (fn-with-deps-keys
                                 nil
                                 [:A]
                                 (fn [{:keys [A]}] (inc A)))
                        :C (fn-with-deps-keys
                                 nil
                                 [:B]
                                 (fn [{:keys [B]}] (inc B)))}}
        (xform/xform :A 9)))

  => {:b 10 :c 11}

  (cache/with-clean-cache
    (-> {:b :B
         :c :C
         ::ht/defaults {:B (fn-with-deps-keys
                                 nil
                                 [:A]
                                 (fn [{:keys [A]}] (inc A)))
                        :C (fn-with-deps-keys
                                 nil
                                 [:A :B]
                                 (fn [{:keys [A B]}] (+ A B)))}}
        (xform/xform :A 9)))

  => {:b 10 :c 19}
  ```
  "
  [doc dep-ks f]
  (vary-meta
   (fn [submap]
     (->> dep-ks
          (map (fn [k]
                 [k (cached-xform-k k submap)]))
          (into submap)
          f))
   assoc
   ::dep-ks dep-ks
   :doc doc))

(defmacro fn-with-deps
  "Shorthand notation for fn-with-deps-impl.

  For example:

  ```clj
  (macroexpand
    '(fn-with-deps nil [A B] (+ A B)))

  =>
  (scicloj.tableplot.v1.dag/fn-with-deps-keys
     scicloj.tableplot.v1.dag/doc
     [:A :B]
     (clojure.core/fn [{:keys [A B]}] (+ A B)))

  (cache/with-clean-cache
    (-> {:b :B
         :c :C
         ::ht/defaults {:B (fn-with-deps nil [A] (inc A))
                        :C (fn-with-deps nil [B] (inc B))}}
        (xform/xform :A 9)))

  => {:b 10 :c 11}
  ```
  "
  ([doc dep-symbols & forms]
   `(fn-with-deps-keys
     ~doc
     ~(mapv #(keyword (name %)) dep-symbols)
     (fn [{:keys ~dep-symbols}]
       ~@forms))))

(defmacro defn-with-deps
  "Defining a function using fn-with-deps-impl.

  For example:

  ```clj
  (macroexpand
    '(defn-with-deps A+B nil [A B] (+ A B)))

  =>
  (def A+B
    (scicloj.tableplot.v1.dag/fn-with-deps nil [A B] (+ A B)))

  (defn-with-deps B->C nil [B] (inc B))
  (defn-with-deps A->B nil [A] (inc A))

  (cache/with-clean-cache
    (-> {:b :B
         :c :C
         ::ht/defaults {:B A->B
                        :C B->C}}
        (xform/xform :A 9)))

  => {:b 10 :c 11}
  ```
  "
  [fsymbol doc dep-symbols & forms]
  `(def ~fsymbol
     (fn-with-deps ~doc
                   ~dep-symbols
                   ~@forms)))
