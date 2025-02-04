(ns scicloj.tableplot.v1.transpile
  (:require [scicloj.kindly.v4.kind :as kind]
            [std.lang :as l]
            [charred.api :as charred]
            [clojure.string :as str]
            [tablecloth.api :as tc]
            [tableplot-book.datasets :as datasets]
            [scicloj.kindly.v4.api :as kindly]))

(defn js
  "Transpile the given Clojure `forms` to Javascript code using [Std.lang](https://clojureverse.org/t/std-lang-a-universal-template-transpiler/)."
  [& forms]
  ((l/ptr :js)
   (cons 'do forms)))

(defn- js-assignment [symbol data]
  (format "let %s = %s;"
          symbol
          (charred/write-json-str data)))

(defn- js-entry-assignment [symbol0 symbol1 symbol2]
  (format "let %s = %s['%s'];"
          symbol0
          symbol1
          symbol2))

(defn- js-closure [js-statements]
  (->> js-statements
       (str/join "\n")
       (format "(function () {\n%s\n})();")))

(def ^:dynamic *base-kindly-options*
  "Base [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  for structures generated by `div-with-script`."
  {:style {:height :auto}})

(defn div-with-script
  "Create a general transpiled data visualization.

  Given a data structure `data`, a form `script`, and a map `kindly-options`
  create a corresponding [Hiccup](https://github.com/weavejester/hiccup)
  structure.

  The structure will be a `:div` with a `:script` element
  that has some Javascript code.

  The code is transpiled from `script` with some variable bindings
  preceeding it:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  defined by deep-merging `kindly-options` into `*base-kindly-options*`."

  ([data script kindly-options]
   (kind/hiccup
    [:div
     [:script
      (js-closure
       (concat
        (when data
          [(js-assignment 'data data)])
        (when (map? data)
          (->> data
               (map (fn [[k v]]
                      (when (symbol? k)
                        (js-entry-assignment k 'data k))))
               (remove nil?)))
        [(apply js script)]))]]
    (kindly/deep-merge *base-kindly-options*
                       kindly-options))))


(defn echarts
  "Given a data structure `data` and a form `form`,
  create a corresponding [Apache Echarts](https://echarts.apache.org/en/index.html)
  data visualization using `div-with-script`.

  The script will include the following:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.
  - The `form` transpiled to Javascript.
  - Additional code to visualize it.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  necessart to make the plot work.

  If no `data` value is passed, it is considered `nil`."
  ([form]
   (echarts nil form))
  ([data form]
   (div-with-script
    data
    ['(var chart
           (echarts.init document.currentScript.parentElement))
     (list 'chart.setOption form)]
    {:style {:height "400px"}
     :html/deps [:echarts]})))


(defn plotly
  "Given a data structure `data` and a form `form`,
  reate a corrseponding [Plotly.js](https://plotly.com/javascript/)
  data visualization using `div-with-script`.

  The script will include the following:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.
  - The `form` transpiled to Javascript.
  - Additional code to visualize it.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  necessart to make the plot work.

  If no `data` value is passed, it is considered `nil`."
  ([form]
   (plotly nil form))
  ([data form]
   (div-with-script
    data
    [(list 'Plotly.newPlot
           'document.currentScript.parentElement
           (:data form)
           (:layout form)
           (:config form))]
    {:html/deps [:plotly]})))

(defn vega-embed
  "Given a data structure `data` and a form `form`,
  create a corresponding [Vega-Embed](https://github.com/vega/vega-embed)
  data visualization using `div-with-script`.

  The script will include the following:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.
  - The `form` transpiled to Javascript.
  - Additional code to visualize it.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  necessart to make the plot work.

  If no `data` value is passed, it is considered `nil`."
  ([form]
   (vega-embed nil form))
  ([data form]
   (div-with-script
    data
    [(list 'vegaEmbed
           'document.currentScript.parentElement
           form)]
    {:html/deps [:vega]})))

(defn highcharts
  "Given a data structure `data` and a form `form`,
  create a corresponding [Highcharts](https://www.highcharts.com/)
  data visualization using `div-with-script`.

  The script will include the following:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.
  - The `form` transpiled to Javascript.
  - Additional code to visualize it.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  necessart to make the plot work.

  If no `data` value is passed, it is considered `nil`."
  ([form]
   (highcharts nil form))
  ([data form]
   (div-with-script
    data
    [(list 'Highcharts.chart
           'document.currentScript.parentElement
           form)]
    {:html/deps [:highcharts]})))


(defn leaflet
  "Given a data structure `data` and a form `form`,
  create a corresponding [Leaflet](https://leafletjs.com/)
  data visualization using `div-with-script`.

  The script will include the following:

  - A Javscript variable called `data` is bound to the `data` value
  converted to JSON.
  - If `data` is a map that has some keys of symbol type, then
  corresponding Javascript variables named by these symbols
  are bound to the corresponding values converted to JSON.
  - The `form` transpiled to Javascript, and is assumed
  to define a function to process a Leaflet map.
  - Additional code to visualize it.

  The resulting structure is marked by the [Kindly](https://scicloj.github.io/kindly-noted/kindly)
  standard as `kind/hiccup` with [Kindly options](https://scicloj.github.io/kindly-noted/kindly.html#passing-options)
  necessart to make the plot work.

  If no `data` value is passed, it is considered `nil`."
  ([form]
   (leaflet nil form))
  ([data form]
   (div-with-script
    data
    [(list 'var 'f form)
     '(var m (L.map document.currentScript.parentElement))
     '(f m)]
    {:html/deps [:leaflet]
     :style {:height "400px"}})))

