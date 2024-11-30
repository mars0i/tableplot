# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [1-beta-1.1] - 2024-12-01
- improved handling of edges in density layers

## [1-beta-1] - 2024-12-01
- replaced the ad-hoc `WrappedValue` daratype with the Clojure `delay` idiom 
(used to avoid the recursive transformations of certain functions)
- plotly: corrected bar width for histograms
- plotly: added support for overlapping histograms
- plotly: added support for density layers
- plotly: added mark-fill support (in use by default for density layers)

## [1-alpha14.1] - 2024-11-15
- updated deps (Hanami, metamorph.ml, Fastmath, Kindly)

## [1-alpha14] - 2024-11-15
- using our own port of Hanami template `xform` function
- avoiding recursion into datasets in `xform`
- avoiding the wrapping of datasets which is unnecessary with the new `xform`
- removing the api dataset wrapper function which is not necessary anymore

## [1-alpha13] - 2024-11-08
- plotly: added support for 3d coordinates (3d scatterplots)

## [1-alpha12] - 2024-11-03
- plotly: added support for configuring the margin with sensible defaults (breaking the previous default behavior)

## [1-alpha11] - 2024-11-02
- initial support for geo coordinates

## [1-alpha10] - 2024-10-20
- renaming to Tableplot
- changing the main API namespaces

## [1-alpha9] - 2024-10-04
- deps update
- plotly - added support to override specific layer data (experimental)
- plotly - extended layer-smooth to use metamorph.ml models and design matrices

## [1-alpha8] - 2024-09-21
- deps update

## [1-alpha7-SNAPSHOT] - 2024-09-13
- plotly - added text & font support
- plotly - bugfix: broken x axis in histograms

## [1-alpha6-SNAPSHOT] - 2024-08-09
- plotly - coordinates support - WIP
- plotlylcoth - styling changes
- plotly - simplified the inference of mode and type a bit
- debugging support - WIP

## [1-alpha5-SNAPSHOT] - 2024-08-06
- added the `plotly` API (experimental) generating [Plotly.js plots](https://plotly.com/javascript/)

## [1-alpha4-SNAPSHOT] - 2024-07-12
- breaking change: substitution keys are `:=abcd`-style rather than `:haclo/abcd`-style
- simplified CSV writing
- more careful handling of datasets - avoiding layer data when they can reuse the toplevel data
- related refactoring
- facets support - WIP

## [1-alpha3-SNAPSHOT] - 2024-06-28
- catching common problems
- bugfix (#1) of type inference after stat

## [1-alpha2-SNAPSHOT] - 2024-06-22
- renamed the `hanami` keyword namespace to `haclo` to avoid confusion

## [1-alpha1-SNAPSHOT] - 2024-06-22
- initial version
