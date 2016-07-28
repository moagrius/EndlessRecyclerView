# EndlessRecyclerView

Endless/infinite RecyclerView with smooth scroll and fling...

![erv2](https://cloud.githubusercontent.com/assets/701344/17218804/c0d99fe4-54ae-11e6-977f-1394c5a40f1b.gif)

Most open source endless `ListView` or `RecyclerView` implementations in Android have poor UX; a scroll or fling action
will stop when the list reaches its current capacity, the more data is loaded - some more sophisticated
implementations might then "peek" the new content, but as often as not it's simply loaded up off screen without
alerting the user.

A correctly configured `EndlessRecyclerView` will behave as if the content were truly endless, and allow scrolling
or flinging as far as the developer instructs.

## How does it work?

Specify a threshold, in pixels.  This is an arbitrary value and can be dynamic - e.g., you could set the threshold
to three times the height of the RecyclerView, updating the value in `onLayout`.  Once a threshold is set,
any scroll operation will measure the existing content against the current scroll and dimension of the recycler view -
if there is not enough content to meet or exceed the threshold value off screen (generally, below), then a callback
is fired with the number of items (generally rows) needed to consume that space entirely: `space to fill /
average size of an item (row)`.  How that callback is handled is up to you - since `RecyclerViews` and `Adapters` are
generally very custom implementations, we just provide a callback on the adapter - the `fill` method, which is
passed a single parameter: the quantity of items the widget thinks is needed to create enough content to reach
the defined threshold.

If your `RecyclerView` is displaying data from a remote server, you can us the technique shown in the demo -
immediately fill the adapter's data set with placeholder values (e.g., nulls, or maybe data items with an
"uninitialized" flag), `notify` the adapter, then send a network request to replace those placeholders when data becomes
 available (and `notify` again).

If your `RecyclerView` is displaying local data, or data sets that can be constructed immediately, then that second
step is unnecessary.

## Installation
```
compile 'com.qozix:endlessrecyclerview:1.0'
```

## Usage
Use `EndlessRecyclerView` in place of a normal `RecyclerView`.  Set your threshold using
 `setVerticalThreshold(int threshold)` for vertical layouts, or the horizontal version for horizontal layouts.
 
For your Adapter, subclass `EndlessAdapter` just as you would a standard `RecyclerView.Adapter`, but with one additional
method defined: `fill(int quantity)`.  This method should handle adding items to your dataset when the user scrolls near
the bounds defined by your threshold.

## Documentation
JavaDocs are [here](http://example.com)