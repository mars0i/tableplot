;; # Walkthrough

;; In this walkthrough, we will demonstrate its main functionality.

;; ## Setup

;; Here we require Hanamicloth's main API namepace
;; as well as those of [Hanami](https://github.com/jsa-aerial/hanami),
;; [Tablecloth](https://scicloj.github.io/tablecloth/),
;; the `datetime` namespace of [dtype-next](https://github.com/cnuernber/dtype-next),
;; and also [Kindly](https://scicloj.github.io/kindly-noted/)
;; (which allows us to specify how values should be visualized).

(ns hanamicloth.walkthrough
  (:require [scicloj.hanamicloth.v1.api :as haclo]
            [aerial.hanami.templates :as ht]
            [tablecloth.api :as tc]
            [tech.v3.datatype.datetime :as datetime]
            [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str])
  (:import java.time.LocalDate))

;; ## Some datasets

;; In this walkthrough, we will use a few datasets from [RDatasets](https://vincentarelbundock.github.io/Rdatasets/articles/data.html).

(defn fetch-dataset [dataset-name]
  (-> dataset-name
      (->> (format "https://vincentarelbundock.github.io/Rdatasets/csv/%s.csv"))
      (tc/dataset {:key-fn (fn [k]
                             (-> k
                                 str/lower-case
                                 (str/replace #"\." "-")
                                 keyword))})
      (tc/set-dataset-name dataset-name)))

(defn compact-view [dataset]
  (-> dataset
      (kind/table {:use-datatables true
                   :datatables {:scrollY 150
                                :searching false
                                :info false}})))

;; ### Edgar Anderson's Iris Data

(defonce iris
  (fetch-dataset "datasets/iris"))

(compact-view iris)

;; ### Motor Trend Car Road Tests

(defonce mtcars
  (fetch-dataset "datasets/mtcars"))

(compact-view mtcars)

;; ### US economic time series

(defonce economics-long
  (fetch-dataset "ggplot2/economics_long"))

(compact-view economics-long)

;; ## Basic usage

;; Let us create a scatter plot from the Iris dataset.
;; We pass a Tablecloth dataset to a Hanamicloth function
;; with a Hanami template.
;; Here we use Hanami's original templates (`ht/chart`)
;; and substitution keys (`:X`, `:Y`, `:MSIZE`).

(-> iris
    (haclo/plot ht/point-chart
                {:X :sepal-width
                 :Y :sepal-length
                 :MSIZE 200}))

;; The resulting plot is displayed correctly,
;; as it is annotated by Kindly.

(-> iris
    (haclo/plot ht/point-chart
                {:X :sepal-width
                 :Y :sepal-length
                 :MSIZE 200})
    meta)

;; The value returned by a `haclo/plot` function
;; is a [Vega-Lite](https://vega.github.io/vega-lite/) spec:

(-> iris
    (haclo/plot ht/point-chart
                {:X :sepal-width
                 :Y :sepal-length
                 :MSIZE 200})
    kind/pprint)

;; By looking at the `:values` key above,
;; you can see that the dataset was implicitly represented as CSV.

;; You can also see that

;; ## Using Hanamicloth templates & defaults

;; Hanamicloth offers its own set of templates and substitution keys.
;; Compared to Hanami's original, it is similar but less sophisticated.
;; Also, it supports the layered grammar which is demonstrated
;; later in this document.

(-> iris
    (haclo/plot haclo/point-chart
                #:haclo{:x :sepal-width
                        :y :sepal-length
                        :mark-size 200}))

(-> iris
    (haclo/plot haclo/point-chart
                #:haclo{:x :sepal-width
                        :y :sepal-length
                        :mark-size 200})
    kind/pprint)

;; You see a slight differnece in the resulting spec:
;; it is defined to be rendered as `:svg` by default.

;; ## Inferring and overriding field types

;; Field [types](https://vega.github.io/vega-lite/docs/type.html) are inferred from the Column type.
;; Here, for example, `:haclo/x` and `:haclo/y` are `:quantitative`, and
;; `:haclo/color` is `:nominal`
;; (and is thus coloured with distinct colours rather than a gradient).

(-> iris
    (haclo/plot haclo/point-chart
                #:haclo{:x :sepal-width
                        :y :sepal-length
                        :color :species
                        :mark-size 200}))

;; On the other hand, in the following example,
;; `:color` is `:quantitative`:

(-> mtcars
    (haclo/plot haclo/point-chart
                #:haclo{:x :mpg
                        :y :disp
                        :color :cyl
                        :mark-size 200}))

;; This can be overridden:

(-> mtcars
    (haclo/plot haclo/point-chart
                #:haclo{:x :mpg
                        :y :disp
                        :color :cyl
                        :color-type :nominal
                        :mark-size 200}))

;; ## More examples

(-> mtcars
    (haclo/plot haclo/boxplot-chart
                #:haclo{:x :cyl
                        :x-type :nominal
                        :y :disp}))

(-> iris
    (haclo/plot haclo/rule-chart
                #:haclo{:x :sepal-width
                        :y :sepal-length
                        :x2 :petal-width
                        :y2 :petal-length
                        :mark-opacity 0.5
                        :mark-size 3
                        :color :species}))

;; ## Time series

;; Let us plot a time series:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/plot haclo/line-chart
                #:haclo{:x :date
                        :y :value
                        :mark-color "purple"}))

;; You see, the `:date` field was correctly inferred to be
;; of the `:temporal` kind.

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/plot haclo/line-chart
                #:haclo{:x :date
                        :y :value
                        :mark-color "purple"})
    kind/pprint)

;; ## Delayed transformation

;; Instead of the `haclo/plot` function, it is possible to used
;; `haclo/base`:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base haclo/line-chart
                #:haclo{:x :date
                        :y :value
                        :mark-color "purple"}))

;; The result is displayed the same way, but the internal representation
;; delays the Hanami transformation of templates.

;; Let us compare the two:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/plot haclo/line-chart
                #:haclo{:x :date
                        :y :value
                        :mark-color "purple"})
    kind/pprint)

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base haclo/line-chart
                #:haclo{:x :date
                        :y :value
                        :mark-color "purple"})
    kind/pprint)

;; The structure returned by `haclo/base` is a Hanami template
;; (with [local defaults](https://github.com/jsa-aerial/hanami?tab=readme-ov-file#template-local-defaults)).
;; When it is displayed, it goes through the Hanami transform
;; to recieve the Vega-Lite spec.

;; When we use base, we can keep processing the template in a pipeline
;; of transformations. We will use it soon with layers.

;; ## Adding layers

;; A base plot does not need to have a specified chart.
;; Instead, we may add layers:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value
                        :mark-color "purple"})
    haclo/layer-line)

;; The substitution keys can also be specified on the layer level:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value})
    (haclo/layer-line #:haclo{:mark-color "purple"}))

;; This allows us to create, e.g., aesthetic differences between layers:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value})
    (haclo/layer-point #:haclo{:mark-color "green"
                               :mark-size 200
                               :mark-opacity 0.1})
    (haclo/layer-line #:haclo{:mark-color "purple"}))

;; We can also skip the base and have everything in the layer:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/layer-line #:haclo{:x :date
                              :y :value
                              :mark-color "purple"}))

;; ## Updating data

;; Using `haclo/update-data`, we may process the dataset
;; during the pipeline, affecting only the layers added further down the pipeline.

;; This functionality is inspired by [ggbuilder](https://github.com/mjskay/ggbuilder)
;; and [metamorph](https://github.com/scicloj/metamorph).

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value})

    (haclo/layer-line #:haclo{:mark-color "purple"})
    (haclo/update-data tc/random 5)
    (haclo/layer-point #:haclo{:mark-color "green"
                               :mark-size 200}))

;; You see, we have lots of data for the lines,
;; but only five random points.

;; ## Processing raw vega-lite

;; During a pipeline, we may call `haclo/plot`
;; to apply the Hanami transform and realize the
;; `Vega-Lite` spec.

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value})

    (haclo/layer-line #:haclo{:mark-color "purple"})
    (haclo/update-data tc/random 5)
    (haclo/layer-point #:haclo{:mark-color "green"
                               :mark-size 200})
    haclo/plot
    kind/pprint)

;; While this in itself does not affect the display of the plot,
;; it allows us to keep editing it as a Vega-Lite spec.
;; For example, let us change the backgound colour this way:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (haclo/base #:haclo{:x :date
                        :y :value})

    (haclo/layer-line #:haclo{:mark-color "purple"})
    (haclo/update-data tc/random 5)
    (haclo/layer-point #:haclo{:mark-color "green"
                               :mark-size 200})
    haclo/plot
    (assoc :background "lightgrey"))

;; ## Smoothing

;; `haclo/layer-smooth` is a layer that applies some statistical
;; processing to the dataset to model it as a smooth shape.
;; It is inspired by ggplot's [geom_smooth](https://ggplot2.tidyverse.org/reference/geom_smooth.html).

;; At the moment, it can only be used to model `:haclo/y` by linear regression.
;; Soon we will add more ways of modelling the data.

(-> iris
    (haclo/base #:haclo{:title "dummy"
                        :mark-color "green"
                        :x :sepal-width
                        :y :sepal-length})
    haclo/layer-point
    (haclo/layer-smooth #:haclo{:mark-color "orange"}))

;; By default, the regression is computed with only one predictor variable,
;; which is `:haclo/x`.
;; But this can be overriden using the `:predictors` key.
;; We may compute a regression with more than one predictor.

(-> iris
    (haclo/base #:haclo{:x :sepal-width
                        :y :sepal-length})
    haclo/layer-point
    (haclo/layer-smooth #:haclo{:predictors [:petal-width
                                             :petal-length]}))

;; ## Grouping

;; The regression computed by `haclo/layer-smooth`
;; is affected by the inferred grouping of the data.

;; For example, here we recieve three regression lines,
;; each for every species.

(-> iris
    (haclo/base #:haclo{:title "dummy"
                        :color :species
                        :x :sepal-width
                        :y :sepal-length})
    haclo/layer-point
    haclo/layer-smooth)

;; This happened because the `:color` field was `:species`,
;; which is of `:nominal` type.

;; But we may override this using the `:group` key.
;; For example, let us avoid grouping:

(-> iris
    (haclo/base #:haclo{:title "dummy"
                        :mark-color "green"
                        :color :species
                        :group []
                        :x :sepal-width
                        :y :sepal-length})
    haclo/layer-point
    haclo/layer-smooth)

;; ## Example: out-of-sample predictions

;; Here is a slighly more elaborate example
;; inpired by the London Clojurians [talk](https://www.youtube.com/watch?v=eUFf3-og_-Y)
;; mentioned in the preface.

;; Assume we wish to predict the unemployment rate for 96 months.
;; Let us add those months to our dataset,
;; and mark them as `Future` (considering the original data as `Past`):

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :days))
                            :relative-time "Future"})))

;; Let us represent our dates as numbers, so that we can use them in linear regression:

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y)))))

;; Let us use the same regression line for the `Past` and `Future` groups.
;; To do this, we avoid grouping by assigning  `[]` to `:haclo/group`.
;; The line is affected only by the past, since in the Future, `:y` is missing.
;; We use the numerical field `:yearmonth` as the regression predictor,
;; but for plotting, we still use the `:temporal` field `:date`.

(-> economics-long
    (tc/select-rows #(-> % :variable (= "unemploy")))
    (tc/add-column :relative-time "Past")
    (tc/concat (tc/dataset {:date (-> economics-long
                                      :date
                                      last
                                      (datetime/plus-temporal-amount (range 96) :months))
                            :relative-time "Future"}))
    (tc/add-column :year #(datetime/long-temporal-field :years (:date %)))
    (tc/add-column :month #(datetime/long-temporal-field :months (:date %)))
    (tc/map-columns :yearmonth [:year :month] (fn [y m] (+ m (* 12 y))))
    (haclo/base #:haclo{:x :date
                        :y :value})
    (haclo/layer-smooth #:haclo{:color :relative-time
                                :mark-size 10
                                :group []
                                :predictors [:yearmonth]})
    ;; Keep only the past for the following layer:
    (haclo/update-data (fn [dataset]
                         (-> dataset
                             (tc/select-rows (fn [row]
                                               (-> row :relative-time (= "Past")))))))
    (haclo/layer-line #:haclo{:mark-color "purple"
                              :mark-size 3}))

;; ## Histograms

;; Histograms can also be represented as layers
;; with statistical processing:

(-> iris
    (haclo/layer-histogram #:haclo{:x :sepal-width}))

(-> iris
    (haclo/layer-histogram #:haclo{:x :sepal-width
                                   :histogram-nbins 30}))
