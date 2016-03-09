'use strict';

angular.module('emr.ui.charts', [])

    .factory('chartService', [function(){

        return {
            clearCanvas: function(context, width, height) { context.clearRect(0, 0, width, height); },

            createCanvas: function(parent, cssClass){

                var canvas = d3.select(parent).append("canvas");
                if(cssClass)
                    canvas.attr("class", cssClass);

                return canvas;
            },

            drawLine: function(context, from, to, color, lineWidth){
                color = color || "#666";
                lineWidth = lineWidth || 1;

                context.strokeStyle = color;
                context.lineWidth = lineWidth;
                context.beginPath();
                context.moveTo(from.x, from.y);
                context.lineTo(to.x , to.y);
                context.stroke();
            },

            drawRect: function(context, from, to, color){
                color = color || "#fff";

                context.fillStyle = color;
                context.fillRect(from.x, from.y, to.x, to.y);
            },

            drawText: function(context, text, position, font, color, textAlign, textBaseline){
                color = color || "#666";
                textAlign = textAlign || "center";
                textBaseline = textBaseline || "middle";

                context.fillStyle = color;
                context.font = font;
                context.textAlign = textAlign;
                context.textBaseline = textBaseline;
                context.fillText(text, position.x, position.y);
            },

            getXScale: function(type){

                var scale;
                switch(type){
                    case "time":
                        scale = d3.time.scale();
                        break;
                    default:
                        scale = d3.scale.linear();
                        break;
                }
                return scale;
            },

            getYScale: function(){ return d3.scale.linear(); }
        };
    }])

    .directive('barChart', ['$timeout', function($timeout) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                values: "="
            },
            link: function ($scope, element, attrs) {

                function draw() {

                    // bail if there is no data
                    if (!$scope.values)
                        return;

                    var margin = { top: 10, right: 10, bottom: 10, left: 10},
                        width = element.width(),
                        height = element.height();

                    var xScale = d3.scale.ordinal()
                        .rangeRoundBands([0, width - margin.left - margin.right], .1)
                        .domain($scope.values.map(function(d, i) { return i; }));

                    var yScale = d3.scale.linear()
                        .range([height - margin.top - margin.bottom, 0])
                        .domain([0, d3.max($scope.values, function(d) { return d; })]);

                    var svg = d3.select(element[0]).append("svg")
                        .attr("width", width)
                        .attr("height", height)
                        .append("g")
                            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                    svg.selectAll("rect").data($scope.values).enter()
                        .append("rect")
                            .attr("fill", "steelblue")
                            .attr("x", function(d, i) { return xScale(i); })
                            .attr("width", xScale.rangeBand())
                            .attr("y", function(d) { return yScale(d); })
                            .attr("height", function(d) { return height - margin.top - margin.bottom - yScale(d); });
                }

                $timeout(function(){
                    draw();
                }, 10);
            }
        }
    }])

    .directive('chart2d', ['chartService', 'colorService', function(chartService, colorService){

        return {
            restrict: 'E',
            replace: false,
            scope: {
                methods: "="
            },
            link: function($scope, element, attrs){

                var margin = { top: 20, right: 16, bottom: 50, left: 50 };
                var tickSize = 5;
                var isRendered = false;
                var options = null,
                    data = null,
                    xAxisData = null;
                var width, height, xScale, yScale, isTimeScale;

                // initialize a canvas for each axis and and the plot area
                var plotArea = chartService.createCanvas(element[0], "plot-area"),
                    yAxis = chartService.createCanvas(element[0], "y-axis"),
                    xAxis = chartService.createCanvas(element[0], "x-axis"),
                    plotAreaContext = plotArea.node().getContext('2d'),
                    yAxisContext = yAxis.node().getContext('2d'),
                    xAxisContext = xAxis.node().getContext('2d');

                $scope.internalMethods = $scope.methods || {};

                /**
                 * Internal methods
                 */

                /**
                 * draw the chart
                 */
                function draw(){

                    // if previously rendered, clear the canvas
                    if (isRendered) {
                        chartService.clearCanvas(plotAreaContext, width, height);
                        chartService.clearCanvas(yAxisContext, margin.left, height);
                        chartService.clearCanvas(xAxisContext, width, margin.bottom);
                    }

                    // exit if no data is present
                    if (!data) return;

                    var markerSize = 4;
                    options.series.forEach(function(series, seriesIndex){

                        // get the series color
                        var color = colorService.getColor(options.colorSet, seriesIndex);

                        // reference the x and y features
                        var yFeature = data[series.y.name];
                        var xFeature = null;
                        if (options.hasXCoordinate)
                            xFeature = data[series.x.name];
                        else if(options.x.type === "TimeStamp")
                            xFeature = xAxisData;

                        var index = 0, n = yFeature.length, y, x;

                        if (options.type == 'scatter'){
                            // draw marker

                            plotAreaContext.fillStyle = color;
                            while (index++ < n) {

                                y = yScale((options.scaled) ? (yFeature[index] - series.y.min) / (series.y.max - series.y.min) : yFeature[index]);
                                x = (xFeature == null) ? xScale(index + 1) : xScale((options.scaled) ? (yFeature[index] - series.y.min) / (series.y.max - series.y.min) : xFeature[index]);

                                plotAreaContext.fillRect(x, y, markerSize, markerSize);
                            }
                        }
                        else{
                            // draw line

                            y = yScale((options.scaled) ? (yFeature[index] - series.y.min) / (series.y.max - series.y.min) : yFeature[index]);
                            x = (xFeature == null) ? xScale(1) : xScale(xFeature[index]);

                            plotAreaContext.strokeStyle = color;
                            plotAreaContext.lineWidth = 1;
                            plotAreaContext.lineCap = "round";
                            plotAreaContext.beginPath();
                            plotAreaContext.moveTo(x, y);
                            while (++index < n) {

                                y = yScale((options.scaled) ? (yFeature[index] - series.y.min) / (series.y.max - series.y.min) : yFeature[index]);
                                x = (xFeature == null) ? xScale(index + 1) : xScale(xFeature[index]);
                                plotAreaContext.lineTo(x, y);
                            }
                            plotAreaContext.stroke();
                        }
                    });

                    // draw axes
                    var t, pos;

                    // y-axis
                    chartService.drawLine(yAxisContext, {x: xScale.range()[0] + margin.left, y: yScale.range()[1] + tickSize}, {x: xScale.range()[0] + margin.left, y: yScale.range()[0]});
                    var ticks = yScale.ticks();
                    for(t = 0; t < ticks.length; t++){

                        pos = { x: xScale.range()[0] + margin.left, y: yScale(ticks[t]) };
                        chartService.drawLine(yAxisContext, pos, {  x: pos.x - tickSize, y: pos.y });
                        chartService.drawText(yAxisContext, ticks[t], {  x: pos.x - tickSize - 2, y: pos.y }, "12px Arial", "#666", "end", "middle");
                    }

                    // x-axis
                    chartService.drawLine(xAxisContext, {x: xScale.range()[0] - tickSize, y: 0}, {x: xScale.range()[1] + tickSize, y: 0});

                    var tickCount = null;
                    if (isTimeScale)
                        tickCount = 8;
                    ticks = xScale.ticks(tickCount);
                    for(t = 0; t < ticks.length; t++){

                        pos = { x: xScale(ticks[t]) + tickSize, y: 0 };
                        chartService.drawLine(xAxisContext, pos, {  x: pos.x, y: pos.y + tickSize });

                        if (isTimeScale){
                            var date = new Date(ticks[t]*1000);
                            var dateText = date.toLocaleDateString('en-US', {timeZone: "UTC", year: "numeric", month: "2-digit", day: "2-digit"});
                            var timeText = date.toLocaleTimeString('en-US', {timeZone: "UTC", hour12: false, hour: "2-digit", minute: "2-digit", second: "2-digit"});
                            chartService.drawText(xAxisContext, dateText, {  x: pos.x, y: pos.y + tickSize + 2 }, "12px Arial", "#666", "center", "top");
                            chartService.drawText(xAxisContext, timeText, {  x: pos.x, y: pos.y + tickSize + 14 }, "12px Arial", "#666", "center", "top");
                        }
                        else{
                            chartService.drawText(xAxisContext, ticks[t], {  x: pos.x, y: pos.y + tickSize + 2 }, "12px Arial", "#666", "center", "top");
                        }
                    }

                    isRendered = true;
                }

                /**
                 * set the canvas size based on the parent
                 */
                function setCanvasSize(){
                    // capture the parent's width and height
                    width = element.width();
                    height = element.height();

                    // set the size of each plot canvas
                    plotArea.attr("width", width - margin.left)
                        .attr("height", height - margin.bottom);
                    yAxis.attr("width", margin.left)
                        .attr("height", height + tickSize - margin.bottom);
                    xAxis.attr("width", width + tickSize - margin.left)
                        .attr("height", margin.bottom);
                }

                /**
                 * set the chart options
                 */
                function setOptions(){

                    /*
                    * Options schema
                    options = {
                        type: "line",
                        scaled: false,
                        hasXCoordinate: false,
                        colorSet: "default",
                        x: { type: "Index", min: null, max: null },
                        y: { min: null, max: null },
                        series: []
                    };
                    */

                    if (!data) return;

                    var yMin = 0,
                        yMax = 1,
                        xMin = 0,
                        xMax = 1;

                    if (!options.scaled){
                        yMin = options.y.min || 0;
                        yMax = options.y.max || 1;
                    }

                    // capture the chart boundaries
                    if (options.x.type === "TimeStamp" && xAxisData !== null){

                        // capture the time series min and max value
                        xMin = xAxisData[0];
                        xMax = xAxisData[xAxisData.length - 1];

                        isTimeScale = true;
                    }
                    else if (!options.hasXCoordinate || !options.scaled){

                        xMin = options.x.min || 0;
                        xMax = options.x.max || 1;
                    }

                    var xScaleType = (isTimeScale) ? "time" : "linear";
                    xScale = chartService.getXScale(xScaleType)
                        .range([0, width - (margin.left + margin.right)])
                        .domain([xMin, xMax])
                        .nice();

                    yScale = chartService.getYScale()
                        .range([margin.top, height - margin.bottom])
                        .domain([yMax, yMin])
                        .nice();

                    // configure x and y axis zooming
                    yAxis.call(d3.behavior.zoom()
                        .y(yScale)
                        .on("zoom", draw));
                    xAxis.call(d3.behavior.zoom()
                        .x(xScale)
                        .on("zoom", draw));
                }

                /**
                 * set chart variables and render
                 */
                $scope.internalMethods.render = function(chartOptions, axisData, chartData){

                    options = chartOptions;
                    xAxisData = axisData;
                    data = chartData;

                    setCanvasSize();
                    setOptions();
                    draw();
                };
            }
        }
    }])

    .directive('exploreChart', ['$timeout', 'colorService', function($timeout, colorService){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/charts/exploreChart.html',
            scope: {
                features: "=",
                onFetch: "="
            },
            link: {

                pre: function($scope, element, attrs){

                    // initialize the chart methods object
                    $scope.chartMethods = {};
                },
                post: function($scope, element, attrs){

                    // initialize variables to track feature counts and the chart data
                    var chartFeatures = {},
                        chartData = {},
                        xAxisData = null;

                    $scope.menuClosed = false;

                    // initialize the chart object
                    $scope.chartOptions = {
                        type: "line",
                        scaled: false,
                        hasXCoordinate: false,
                        colorSet: "default",
                        x: { type: "Index", min: null, max: null },
                        y: { min: null, max: null },
                        series: []
                    };

                    // initialize new series object
                    $scope.newSeries = { x: { name: null }, y: { name: null } };

                    // initialize the list of xlabels
                    $scope.xAxisOptions = ["Index"];

                    /**
                     * on features change, update the list of available timestamps based whether a timestamp feature exists
                     */
                    $scope.$watch("features", function(){

                        var index = timestampIndex();
                        if (index > -1){ // a timestamp exists - update the list of xLabels
                            $scope.xAxisOptions = ["Index", "TimeStamp"];
                        }
                        else{ // no timestamp feature
                            $scope.xAxisOptions = ["Index"];
                        }
                    });

                    //
                    // private methods
                    //


                    function fetchData(){

                        var featureSet = [];
                        for (var chartFeature in chartFeatures) {
                            if (chartFeatures.hasOwnProperty(chartFeature) && chartData[chartFeature] === undefined) {
                                var feature = findFeature(chartFeature);
                                featureSet.push(feature);
                            }
                        }

                        if (featureSet.length > 0){

                            $scope.onFetch(featureSet, function(data){

                                var features = data.features;
                                for(var index = 0; index < features.length; index++)
                                    chartData[features[index].name] = features[index].data;

                                $scope.chartMethods.render($scope.chartOptions, xAxisData, chartData);
                            });
                        }
                    }

                    function findFeature(name){
                        var index = findFeatureIndex(name);
                        return $scope.features[index];
                    }

                    function findFeatureIndex(name){
                        return $scope.features.map(function(feature) { return feature.name; }).indexOf(name);
                    }

                    function incrementChartFeature(name){
                        if (chartFeatures[name] === undefined)
                            chartFeatures[name] = 0;
                        chartFeatures[name] += 1;
                    }

                    function decrementChartFeature(name){
                        // decrement chart feature count
                        chartFeatures[name] -= 1;
                        // remove if count drops to zero
                        if (chartFeatures[name] == 0)
                            delete chartFeatures[name];
                    }

                    function timestampIndex(){
                        return $scope.features.map(function(feature) { return feature.type; }).indexOf("timestamp");
                    }

                    function render(){

                    }

                    function resetSeries() {

                        $scope.newSeries.y.name = null;
                        $scope.newSeries.x.name = null;
                    }

                    //
                    // public methods
                    //

                    /**
                     * Adds the new series to the list of chart series
                     */
                    $scope.addSeries = function(){

                        // verify the series has been properly configured
                        if ($scope.newSeries.y.name == null) return;
                        if ($scope.chartOptions.hasXCoordinate && $scope.newSeries.x.name == null) return;

                        // copy the new series and append to the list of series
                        var series = angular.copy($scope.newSeries);
                        series.flipped = false;

                        // y feature
                        var feature = findFeature(series.y.name);
                        // increment chart feature count
                        incrementChartFeature(feature.name);

                        // update chart boundaries
                        series.y.max = feature.max;
                        series.y.min = feature.min;
                        series.y.count = feature.count;
                        if ($scope.chartOptions.y.max == null || series.y.max > $scope.chartOptions.y.max)
                            $scope.chartOptions.y.max = series.y.max;
                        if ($scope.chartOptions.y.min == null || series.y.min < $scope.chartOptions.y.min)
                            $scope.chartOptions.y.min = series.y.min;

                        if ($scope.chartOptions.hasXCoordinate){

                            // x feature
                            feature = findFeature(series.x.name);
                            incrementChartFeature(feature.name);

                            series.x.max = feature.max;
                            series.x.min = feature.min;
                            series.x.count = feature.count;
                            if ($scope.chartOptions.x.max == null || series.x.max > $scope.chartOptions.x.max)
                                $scope.chartOptions.x.max = series.x.max;
                            if ($scope.chartOptions.x.min == null || series.x.min < $scope.chartOptions.x.min)
                                $scope.chartOptions.x.min = series.x.min;
                        }
                        else if ($scope.chartOptions.x.type === "Index") {

                            // this chart is plotted against the data's index
                            // use the feature counts to update the x -min and -max values

                            $scope.chartOptions.x.min = 0;
                            if ($scope.chartOptions.x.max == null || series.y.count > $scope.chartOptions.x.max)
                                $scope.chartOptions.x.max = series.y.count;
                        }

                        $scope.chartOptions.series.push(series);

                        resetSeries();

                        fetchData();
                    };

                    /**
                     * returns the color associated with the specified index
                     * @param index
                     * @returns {*}
                     */
                    $scope.getColor = function(index) {
                        return colorService.getColor($scope.chartOptions.colorSet, index);
                    };

                    /**
                     * called when chart type is updated, adjusts chart configuration based on chart type change
                     */
                    $scope.onChartTypeUpdate = function() {

                        // set whether chart has x coordinate
                        $scope.chartOptions.hasXCoordinate = ($scope.chartOptions.type == "scatter");
                    };

                    /**
                     * Flips the series selector
                     * @param item
                     * @param isFlipped
                     */
                    $scope.onFlipCheck = function(item, isFlipped){
                        item.flipped = isFlipped;
                    };

                    /**
                     * remove a specific series
                     * @param item
                     */
                    $scope.onRemoveSeries = function(item){

                        // remove the specified series
                        $scope.chartOptions.series.splice($scope.chartOptions.series.indexOf(item), 1);

                        // decrement chart feature count
                        chartFeatures[item.y.name] -= 1;
                        // remove if count drops to zero
                        if (chartFeatures[item.y.name] == 0)
                            delete chartFeatures[item.y.name];

                        // if applicable, decrement the x coordinate
                        if ($scope.chartOptions.hasXCoordinate){
                            chartFeatures[item.x.name] -= 1;
                            // remove if count drops to zero
                            if (chartFeatures[item.x.name] == 0)
                                delete chartFeatures[item.x.name];
                        }
                    };

                    /**
                     * called when y-axis scale option is toggled, adjusts chart configuration based on change
                     */
                    $scope.onToggleScale = function() {

                    };

                    /**
                     * Show / hides explore chart menu
                     */
                    $scope.onToggleMenu = function() {
                        $scope.menuClosed = !$scope.menuClosed;

                        $timeout(function(){
                            $scope.chartMethods.render($scope.chartOptions, xAxisData, chartData);
                        }, 10);
                    };

                    /**
                     * called when the xlabel dropdown is modified
                     */
                    $scope.onXLabelUpdate = function(){

                        if ($scope.chartOptions.x.type === "TimeStamp") {

                            var index = timestampIndex();
                            var fetchRequest = [];
                            fetchRequest.push($scope.features[index]);
                            $scope.onFetch(fetchRequest, function(data){

                                xAxisData = data.features[0].data;
                            });
                        }
                        else{
                            xAxisData = null;
                        }
                    };
                }
            }
        }
    }])

    .directive('progressCircle', ['$timeout', function($timeout){

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/charts/progressCircle.html',
            scope: {
                backgroundColor: "@",
                foregroundColor: "@",
                showValue: "@?",
                progress: "="
            },
            link: function($scope, element, attrs){

                var padding = 12;

                if ($scope.showValue === undefined)
                    $scope.showValue = true;

                $scope.radius = (element.width() - padding) / 2;
                $scope.centerX = element.width() / 2;
                $scope.centerY = element.height() / 2;

                attrs.$observe('showValue', function() {
                    $scope.showValue = $scope.$eval(attrs.showValue);
                });

                $scope.$watch("progress", function(newValue, oldValue){
                    var perimeter = $scope.perimeter($scope.radius);
                    $scope.strokeOffset = perimeter - perimeter * newValue / 100;
                });

                $scope.circlePath = function(radius, centerX, centerY){
                    return "M " + centerX  + " " + centerY + " m 0,-" + radius + " a " + radius + ","
                        + radius + " 0 1,1 0," + (radius * 2) + " a " + radius + "," + radius + " 0 1,1 0,-" + (radius * 2);
                };

                function init(){

                    $scope.strokeOffset = $scope.perimeter($scope.radius);
                    $scope.progressValue = 0;

                    // set the
                    $timeout(function(){
                        element.children("svg")
                            .children("g")
                            .children(".progress-fg")
                            .css("transition", "stroke-dashoffset 0.2s");
                    }, 10);
                }

                $scope.perimeter = function(radius) {
                    return 2 * Math.PI * radius;
                };

                init();
            }
        }
    }])

    .directive('trendLine', ['chartService', '$location',  function(chartService, $location){

        return {
            restrict: 'E',
            replace: false,
            scope: {
                data: "=trendData",
                size: "="
            },
            link: function($scope, element, attrs){

                // initialize trend line variables
                var margin = { top: 20, right: 16, bottom: 50, left: 50 };
                var width,
                    height,
                    yMin = 0,
                    yMax = 3,
                    capacity = $scope.size - 1;

                // initialize chart data and scales
                var chartData = $scope.data.map(function(value, index) { return { x: index, y: value }; });
                var xScale = chartService.getXScale().domain([0, capacity-1]);
                var yScale = chartService.getYScale().domain([yMax, yMin]);

                // create line and axes functions
                var line = d3.svg.line()
                    .x(function(d) { return xScale(d.x); })
                    .y(function(d) { return yScale(d.y); });

                var xAxis = d3.svg.axis().scale(xScale).orient("bottom").outerTickSize(0).tickPadding(10);
                var yAxis = d3.svg.axis().scale(yScale).orient("left").outerTickSize(0).tickPadding(10);

                // initialize the chart, canvas, and clip path
                var svg = d3.select(element[0]).append("svg");
                var canvas = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                var clip = canvas.append("defs").append("clipPath").attr("id", "clip").append("rect");

                canvas.append("g").attr("class", "x axis");
                canvas.append("g").attr("class", "y axis");

                var path = canvas.append("g")
                    .attr("clip-path", "url(" + $location.absUrl() + "#clip)")
                    .append("path")
                        .datum(chartData)
                        .attr("class", "trend-line")
                        .attr("d", line);

                // watch for a change in the parent element's size
                $scope.$watch(
                    function () { return { width: element.width(), height: element.height() } },
                    function () {
                        setChartDimensions();
                        redraw();
                    },
                    true
                );

                // watch for a change to the data
                $scope.$watch('data', function (newValue, oldValue) {

                    chartData = $scope.data.map(function(value, index) { return { x: index, y: value }; });
                    redraw();
                }, true);

                function interpolation(data) {

                    return function (d, i, a) {
                        var interpolate = d3.scale.linear()
                            .domain([0,1])
                            .range([data.length - 1, data.length]);

                        return function(t) {
                            var flooredX = Math.floor(interpolate(t));
                            var weight = interpolate(t) - flooredX;
                            var interpolatedLine = data.slice(0, flooredX);

                            if(flooredX > 0 && flooredX < data.length) {
                                var weightedLineAverage = data[flooredX].y * weight + data[flooredX-1].y * (1-weight);
                                interpolatedLine.push({"x":interpolate(t)-1, "y":weightedLineAverage});
                            }

                            return line(interpolatedLine);
                        }
                    }
                }

                function redraw(){

                    if ($scope.data.length == 0)
                        return;

                    if(chartData.length > capacity){
                        path.datum(chartData)
                            .attr("d", line)
                            .attr("transform", null)
                            .transition()
                                .duration(500)
                                .ease("linear")
                                .attr("transform", "translate(" + xScale(-1) + ",0)");
                    }
                    else{
                        path.transition()
                            .duration(500)
                            .attrTween('d', interpolation(chartData));
                    }

                }

                function setChartDimensions(){

                    width = element.width();
                    height = element.height();

                    svg.attr("width", width).attr("height", height);

                    var innerWidth = width - (margin.left + margin.right),
                        innerHeight = height - (margin.top + margin.bottom);

                    xScale.range([0, innerWidth]);
                    yScale.range([0, innerHeight]);

                    clip.attr("width", innerWidth)
                        .attr("height", innerHeight);

                    xAxis.innerTickSize(-innerHeight);
                    yAxis.innerTickSize(-innerWidth);

                    canvas.select(".x.axis").attr("transform", "translate(0," + innerHeight + ")").call(xAxis);
                    canvas.select(".y.axis").call(yAxis);
                }
            }
        }
    }]);

