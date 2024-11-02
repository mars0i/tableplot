;; # Plotly walkthrough 👣 - experimental 🛠

;; Tableplot offers a Clojure API for creating [Plotly.js](https://plotly.com/javascript/) plots through layered pipelines.

;; The API not uses [Hanami templates](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#templates-substitution-keys-and-transformations) but is completely separate from the classical Hanami templates and parameters.

;; Here, we provide a walkthrough of that API.

;; 🛠 This part of Tableplot is still in experimental stage.
;; Some of the details will change soon. Feedback and comments will help.

;; Soon, we will provide more in-depth explanations in additional chapters.

;; ## Known issues

;; - Plot legends are missing in some cases.

;; - Breaking changes are expected.

;; ## Setup
;; For this tutorial, we require:

;; * The Tablecloth plotly API namepace

;; * [Tablecloth](https://scicloj.github.io/tablecloth/) for dataset processing

;; * the [datetime namespace](https://cnuernber.github.io/dtype-next/tech.v3.datatype.datetime.html) of [dtype-next](https://github.com/cnuernber/dtype-next)

;; * the [print namespace](https://techascent.github.io/tech.ml.dataset/tech.v3.dataset.print.html) of [tech.ml.dataset](https://github.com/techascent/tech.ml.dataset) for customized dataset printing

;; * [Kindly](https://scicloj.github.io/kindly-noted/) (to specify how certaiun values should be visualized)

;; * the datasets defined in the [Datasets chapter](./tableplot_book.datasets.html)

(ns tableplot-book.plotly-walkthrough
  (:require [scicloj.tableplot.v1.plotly :as plotly]
            [tablecloth.api :as tc]
            [tablecloth.column.api :as tcc]
            [tech.v3.datatype.datetime :as datetime]
            [tech.v3.dataset.print :as print]
            [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]
            [scicloj.kindly.v4.api :as kindly]
            [tableplot-book.datasets :as datasets]
            [aerial.hanami.templates :as ht]))

;; ## Basic usage

;; Plotly plots are created by passing datasets to a pipeline
;; of layer functions.

;; Additional parameters to the functions are passed as maps.
;; Map keys begin with `=` (e.g., `:=color`).

;; For example, let us plot a scatterplot (a layer of points)
;; of 10 random items from the Iris dataset.

(-> datasets/iris
    (tc/random 10 {:seed 1})
    (plotly/layer-point
     {:=x :sepal-width
      :=y :sepal-length
      :=color :species
      :=mark-size 20
      :=mark-opacity 0.6}))

;; ## Templates and parameters

;; (💡 You do neet need to understand these details for basic usage.)

;; Technically, the parameter maps contain [Hanami substitution keys](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#templates-substitution-keys-and-transformations),
;; which means they are processed by a [simple set of rules](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#basic-transformation-rules),
;; but you do not need to understand what this means yet.

;; The layer functions return a Hanami template. Let us print the resulting
;; structure of the previous plot.

(def example1
  (-> datasets/iris
      (tc/random 10 {:seed 1})
      (plotly/layer-point
       {:=x :sepal-width
        :=y :sepal-length
        :=color :species
        :=mark-size 20
        :=mark-opacity 0.6})))

(kind/pprint example1)

;; This template has all the necessary knowledge, including the substitution
;; keys, to turn into a plot. This happens when your visual tool (e.g., Clay)
;; displays the plot. The tool knows what to do thanks to the Kindly metadata
;; and a special function attached to the plot.

(meta example1)

(:kindly/f example1)

;; ## Realizing the plot

;; If you wish to see the resulting plot specification before displaying it
;; as a plot, you can use the `plot` function. In this case,
;; it generates a Plotly.js plot:

(-> example1
    plotly/plot
    kind/pprint)

;; It is annotated as `kind/plotly`, so that visual tools know how to
;; render it.

(-> example1
    plotly/plot
    meta)

;; This can be useful if you wish to process the Actual Plotly.js spec
;; rather than use the Tableplot Plotly API. Let us change the background colour,
;; for example:

(-> example1
    plotly/plot
    (assoc-in [:layout :plot_bgcolor] "#eeeedd"))

;; For another example, let us use a logarithmic scale for the y axis:
(-> example1
    plotly/plot
    (assoc-in [:layout :yaxis :type] "log"))

;; ## Field type inference

;; Tableplot infers the type of relevant fields from the data.

;; The example above was colored as it were since `:species`
;; column was nominal, so it was assigned distinct colours.

;; In the following example, the coloring is by a quantitative
;; column, so a color gradient is used:

(-> datasets/mtcars
    (plotly/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=mark-size 20}))

;; We can override the inferred types and thus affect the generated plot:

(-> datasets/mtcars
    (plotly/layer-point
     {:=x :mpg
      :=y :disp
      :=color :cyl
      :=color-type :nominal
      :=mark-size 20}))

;; ## More examples

;; ### Boxplot

(-> datasets/mtcars
    (plotly/layer-boxplot
     {:=x :cyl
      :=y :disp}))

;; ### Bar chart

(-> datasets/mtcars
    (tc/group-by [:cyl])
    (tc/aggregate {:total-disp #(-> % :disp tcc/sum)})
    (plotly/layer-bar
     {:=x :cyl
      :=y :total-disp}))

;; ### Text

(-> datasets/mtcars
    (plotly/layer-text
     {:=x :mpg
      :=y :disp
      :=text :cyl
      :=mark-size 20}))

(-> datasets/mtcars
    (plotly/layer-text
     {:=x :mpg
      :=y :disp
      :=text :cyl
      :=textfont {:family "Courier New, monospace"
                  :size 16
                  :color :purple}
      :=mark-size 20}))

;; ### Segment plot

(-> datasets/iris
    (plotly/layer-segment
     {:=x0 :sepal-width
      :=y0 :sepal-length
      :=x1 :petal-width
      :=y1 :petal-length
      :=mark-opacity 0.4
      :=mark-size 3
      :=color :species}))

;; ## Varying color and size

(-> {:ABCD (range 1 11)
     :EFGH [5 2.5 5 7.5 5 2.5 7.5 4.5 5.5 5]
     :IJKL [:A :A :A :A :A :B :B :B :B :B]
     :MNOP [:C :D :C :D :C :D :C :D :C :D]}
    tc/dataset
    (plotly/base {:=title "IJKLMNOP"})
    (plotly/layer-point {:=x :ABCD
                         :=y :EFGH
                         :=color :IJKL
                         :=size :MNOP
                         :=name "QRST1"})
    (plotly/layer-line
     {:=title "IJKL MNOP"
      :=x :ABCD
      :=y :ABCD
      :=name "QRST2"
      :=mark-color "magenta"
      :=mark-size 20
      :=mark-opacity 0.2}))

;; ## Time series

;; Date and time fields are handle appropriately.
;; Let us, for example, draw the time series of unemployment counts.

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/layer-line
     {:=x :date
      :=y :value
      :=mark-color "purple"}))

;; ## Multiple layers

;; We can draw more than one layer:

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/layer-point {:=x :date
                         :=y :value
                         :=mark-color "green"
                         :=mark-size 20
                         :=mark-opacity 0.5})
    (plotly/layer-line {:=x :date
                        :=y :value
                        :=mark-color "purple"}))

;; We can also use the `base` function for the common parameters
;; across layers:

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/base {:=x :date
                  :=y :value})
    (plotly/layer-point {:=mark-color "green"
                         :=mark-size 20
                         :=mark-opacity 0.5})
    (plotly/layer-line {:=mark-color "purple"}))


;; Layers can be named:

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/base {:=x :date
                  :=y :value})
    (plotly/layer-point {:=mark-color "green"
                         :=mark-size 20
                         :=mark-opacity 0.5
                         :=name "points"})
    (plotly/layer-line {:=mark-color "purple"
                        :=name "line"}))

;; ## Updating data

;; We can use the `update-data` function to vary the
;; dataset along a plotting pipeline, affecting
;; the layers that follow.

;; This functionality is inspired by [ggbuilder](https://github.com/mjskay/ggbuilder)
;; and [metamorph](https://github.com/scicloj/metamorph).

;; Here, for example, we draw a line,
;; then sample 5 data rows,
;; and draw them as points:

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (plotly/base {:=x :date
                  :=y :value})
    (plotly/layer-line {:=mark-color "purple"})
    (plotly/update-data tc/random 5)
    (plotly/layer-point {:=mark-color "green"
                         :=mark-size 15
                         :=mark-opacity 0.5}))

;; ## Overriding specific layer data (experimental)

(-> (tc/dataset {:x (range 4)
                 :y [1 2 5 9]})
    tc/dataset
    (tc/sq :y :x)
    (plotly/layer-point {:=mark-size 20})
    (plotly/layer-line {:=dataset (plotly/dataset {:x [0 3]
                                                   :y [1 10]})
                        :=mark-size 5}))

;; ## Smoothing

;; `layer-smooth` is a layer that applies some statistical
;; processing to the dataset to model it as a smooth shape.
;; It is inspired by ggplot's [geom_smooth](https://ggplot2.tidyverse.org/reference/geom_smooth.html).

;; At the moment, it can only be used to model `:=y` by linear regression.
;; Soon we will add more ways of modelling the data.

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=mark-color "orange"
                          :=name "Predicted"}))

;; By default, the regression is computed with only one predictor variable,
;; which is `:=x`.
;; But this can be overriden using the `:predictors` key.
;; We may compute a regression with more than one predictor.

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=predictors [:petal-width
                                        :petal-length]
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

;; We can also provide the design matrix.

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=design-matrix [[:sepal-width '(identity sepal-width)]
                                           [:sepal-width-2 '(* sepal-width
                                                               sepal-width)]]
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

;; Inspired by Sami Kallinen's [Heart of Clojure talk](https://2024.heartofclojure.eu/talks/sailing-with-scicloj-a-bayesian-adventure/):

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=design-matrix [[:sepal-width '(identity sepal-width)]
                                           [:sepal-width-2 '(* sepal-width
                                                               sepal-width)]
                                           [:sepal-width-3 '(* sepal-width
                                                               sepal-width
                                                               sepal-width)]]
                          :=mark-opacity 0.5
                          :=name "Predicted"}))

;; We can also provide the regression model details as metamorph.ml options:

(require 'scicloj.ml.tribuo)

(def regression-tree-options
  {:model-type :scicloj.ml.tribuo/regression
   :tribuo-components [{:name "cart"
                        :type "org.tribuo.regression.rtree.CARTRegressionTrainer"
                        :properties {:maxDepth "8"
                                     :fractionFeaturesInSplit "1.0"
                                     :seed "12345"
                                     :impurity "mse"}}
                       {:name "mse"
                        :type "org.tribuo.regression.rtree.impurity.MeanSquaredError"}]
   :tribuo-trainer-name "cart"})

(-> datasets/iris
    (plotly/base {:=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=mark-color "green"
                         :=name "Actual"})
    (plotly/layer-smooth {:=model-options regression-tree-options
                          :=mark-opacity 0.5
                          :=name "Predicted"}))


;; An example inspired by Plotly's
;; [ML Regressoin in Python](https://plotly.com/python/ml-regression/)
;; example.

(-> datasets/tips
    (tc/split :holdout {:seed 1})
    (plotly/base {:=x :total_bill
                  :=y :tip})
    (plotly/layer-point {:=color :$split-name})
    (plotly/update-data (fn [ds]
                          (-> ds
                              (tc/select-rows #(-> % :$split-name (= :train))))))
    (plotly/layer-smooth {:=model-options regression-tree-options
                          :=name "prediction"
                          :=mark-color "purple"}))


;; ## Grouping

;; The regression computed by `layer-smooth`
;; is affected by the inferred grouping of the data.

;; For example, here we recieve three regression lines,
;; each for every species.

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=color :species
                  :=x :sepal-width
                  :=y :sepal-length})
    plotly/layer-point
    plotly/layer-smooth)

;; This happened because the `:color` field was `:species`,
;; which is of `:nominal` type.

;; But we may override this using the `:group` key.
;; For example, let us avoid grouping:

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=color :species
                  :=group []
                  :=x :sepal-width
                  :=y :sepal-length})
    plotly/layer-point
    plotly/layer-smooth)

;; Alternatively, we may assign the `:=color` only to the points layer
;; without affecting the smoothing layer.

(-> datasets/iris
    (plotly/base {:=title "dummy"
                  :=x :sepal-width
                  :=y :sepal-length})
    (plotly/layer-point {:=color :species})
    (plotly/layer-smooth {:=name "Predicted"
                          :=mark-color "blue"}))

;; ## Example: out-of-sample predictions

;; Here is a slighly more elaborate example
;; inpired by the London Clojurians [talk](https://www.youtube.com/watch?v=eUFf3-og_-Y)
;; mentioned in the preface.

;; Assume we wish to predict the unemployment rate for 96 months.
;; Let us add those months to our dataset,
;; and mark them as `Future` (considering the original data as `Past`):

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :days))
                            :relative-time "Future"}))
    (print/print-range 6))

;; Let us represent our dates as numbers, so that we can use them in linear regression:

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y))))
    (print/print-range 6))

;; Let us use the same regression line for the `Past` and `Future` groups.
;; To do this, we avoid grouping by assigning  `[]` to `:=group`.
;; The line is affected only by the past, since in the Future, `:=y` is missing.
;; We use the numerical field `:yearmonth` as the regression predictor,
;; but for plotting, we still use the `:temporal` field `:date`.

(-> datasets/economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> datasets/economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y))))
    (plotly/base {:=x :date
                  :=y :value})
    (plotly/layer-smooth {:=color :relative-time
                          :=mark-size 15
                          :=group []
                          :=predictors [:yearmonth]})
    ;; Keep only the past for the following layer:
    (plotly/update-data (fn [dataset]
                          (-> dataset
                              (tc/select-rows (fn [row]
                                                (-> row :relative-time (= "Past")))))))
    (plotly/layer-line {:=mark-color "purple"
                        :=mark-size 3
                        :=name "Actual"}))

;; ## Histograms

;; Histograms can also be represented as layers
;; with statistical processing:

(-> datasets/iris
    (plotly/layer-histogram {:=x :sepal-width}))

(-> datasets/iris
    (plotly/layer-histogram {:=x :sepal-width
                             :=histogram-nbins 30}))

;; ## Coordinates
;; (WIP)

;; ### geo

;; Inspired by Plotly's tutorial for [Scatter Plots on Maps in JavaScript](https://plotly.com/javascript/scatter-plots-on-maps/):

(-> {:lat [45.5, 43.4, 49.13, 51.1, 53.34, 45.24,
           44.64, 48.25, 49.89, 50.45]
     :lon [-73.57, -79.24, -123.06, -114.1, -113.28,
           -75.43, -63.57, -123.21, -97.13, -104.6]
     :text ["Montreal", "Toronto", "Vancouver", "Calgary", "Edmonton",
            "Ottawa", "Halifax", "Victoria", "Winnepeg", "Regina"],}
    tc/dataset
    (plotly/base {:=coordinates :geo
                  :=lat :lat
                  :=lon :lon})
    (plotly/layer-point {:=mark-opacity 0.5
                         :=mark-color "grey"
                         :=mark-size 10})
    (plotly/layer-text {:=text :text
                        :=textfont {:size 7
                                    :color :purple}})
    plotly/plot
    (assoc-in [:layout :geo]
              {:scope "north america"
               :resolution 10
               :lonaxis {:range [-130 -55]}
               :lataxis {:range [40 60]}
               :countrywidth 1.5
               :showland true
               :showlakes true
               :showrivers true}))

;; ### 3d
;; (coming soon)


;; ### polar

;; Monthly rain amounts - polar bar-chart

(def rain-data
(tc/dataset
 {:month [:Jan :Feb :Mar :Apr
          :May :Jun :Jul :Aug
          :Sep :Oct :Nov :Dec]
  :rain (repeatedly #(rand-int 200))}))

(-> rain-data
(plotly/layer-bar
     {:=r :rain
      :=theta :month
      :=coordinates :polar
      :=mark-size 20
      :=mark-opacity 0.6}))

;; Controlling the polar layout
;; (by manipulating the raw Plotly.js spec):

(-> rain-data
    (plotly/base
     {})
    (plotly/layer-bar
     {:=r :rain
      :=theta :month
      :=coordinates :polar
      :=mark-size 20
      :=mark-opacity 0.6})
    plotly/plot
    (assoc-in [:layout :polar]
              {:angularaxis {:tickfont {:size 16}
                             :rotation 90
                             :direction "counterclockwise"}
               :sector [0 180]}))

;; A polar random walk - polar line-chart

(let [n 50]
  (-> {:r (->> (repeatedly n #(- (rand) 0.5))
               (reductions +))
       :theta (->> (repeatedly n #(* 10 (rand)))
                   (reductions +)
                   (map #(rem % 360)))
       :color (range n)}
      tc/dataset
      (plotly/layer-point
       {:=r :r
        :=theta :theta
        :=coordinates :polar
        :=mark-size 10
        :=mark-opacity 0.6})
      (plotly/layer-line
       {:=r :r
        :=theta :theta
        :=coordinates :polar
        :=mark-size 3
        :=mark-opacity 0.6})))

;; ## Debugging (WIP)

;; ### Viewing the computational dag of substitution keys:

(def example-to-debug
  (-> datasets/iris
      (tc/random 10 {:seed 1})
      (plotly/layer-point {:=x :sepal-width
                           :=y :sepal-length
                           :=color :species})))

(-> example-to-debug
    plotly/dag)

;; ### Viewing intermediate values in the computational dag:

;; Layers (tableplot's intermediate data representation)

(-> example-to-debug
    (plotly/debug :=layers))

;; Traces (part of the Plotly spec)

(-> example-to-debug
    (plotly/debug :=traces))

;; Both

(-> example-to-debug
    (plotly/debug {:layers :=layers
                   :traces :=traces}))

;; ## Coming soon

;; ### Facets
;; (coming soon)

;; ### Scales
;; (coming soon)
