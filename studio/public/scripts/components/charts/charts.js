'use strict';

angular.module('emr.ui.charts', [])

    .factory('chartService', [function(){

        return {
            clearCanvas: function(context, width, height) { context.clearRect(0, 0, width, height); },

            createCanvas: function(parent){ return d3.select(parent).append("canvas"); },

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
                if (type == 'time')
                    scale = d3.time.scale();
                else
                    scale = d3.scale.linear();

                return scale;
            },

            getYScale: function(type){ return d3.scale.linear(); }
        };
    }])

    .directive('chart2d', ['chartService', 'colorService', function(chartService, colorService){

        return {
            restrict: 'E',
            replace: false,
            scope: {
                methods: "="
            },
            link: function($scope, element, attrs){

                // initialize the chart canvas
                var canvas = chartService.createCanvas(element[0]);
                var context = canvas.node().getContext('2d');
                var margin = { top: 16, right: 16, bottom: 50, left: 50 };
                var tickSize = 5;
                var isRendered = false;
                var data, options, chartType, width, height, xScale, yScale;
                var featureIndex = 1;
                var featureOffset = 2;

                /**
                 * Internal methods
                 */

                /**
                 *
                 */
                function draw(){

                    if (isRendered)
                        chartService.clearCanvas(context, width, height);

                    if (!data) return;

                    var markerSize = 4;
                    options.series.forEach(function(series, seriesIndex){

                        // get the series color
                        var color = colorService.getColor(options.colorSet, seriesIndex);

                        // reference the x and y features
                        var yFeature = data[data[0].indexOf(series.y.name)  + featureOffset];
                        var xFeature = null;
                        if (chartType == 'time')
                            xFeature = data[featureIndex];
                        else if(series.x != null)
                            xFeature = data[data[0].indexOf(series.x.name) + featureOffset];

                        var index = 0, n = yFeature.length, y, x;

                        if (options.type == 'scatter'){
                            // draw marker

                            context.fillStyle = color;
                            while (index++ < n) {

                                y = yScale((options.scaled) ? (parseFloat(yFeature[index]) - series.y.min) / (series.y.max - series.y.min) : parseFloat(yFeature[index]));
                                x = (xFeature == null) ? xScale(index + 1) : xScale((options.scaled) ? (parseFloat(yFeature[index]) - series.y.min) / (series.y.max - series.y.min) : parseFloat(xFeature[index]));

                                context.fillRect(x, y, markerSize, markerSize);
                                /*context.beginPath();
                                context.arc(x, y, markerSize, 0, 2 * Math.PI);
                                context.fill();*/
                            }
                        }
                        else{
                            // draw line

                            y = yScale((options.scaled) ? (parseFloat(yFeature[index]) - series.y.min) / (series.y.max - series.y.min) : parseFloat(yFeature[index]));
                            x = (xFeature == null) ? xScale(1) : xScale(xFeature[index]);

                            context.strokeStyle = color;
                            context.lineWidth = 1;
                            context.lineCap = "round";
                            context.beginPath();
                            context.moveTo(x, y);
                            while (++index < n) {

                                y = yScale((options.scaled) ? (parseFloat(yFeature[index]) - series.y.min) / (series.y.max - series.y.min) : parseFloat(yFeature[index]));
                                x = (xFeature == null) ? xScale(index + 1) : xScale(xFeature[index]);
                                context.lineTo(x, y);
                            }
                            context.stroke();
                        }
                    });

                    // draw axes
                    var origin = { x: xScale.range()[0], y: yScale.range()[1] };

                    // draw axis boundaries
                    chartService.drawRect(context, {x: 0, y: 0}, {x: xScale.range()[0], y: height});
                    chartService.drawRect(context, {x: 0, y: yScale.range()[1]}, {x: width, y: height});

                    // y-axis
                    chartService.drawLine(context, origin, {x: xScale.range()[0], y: yScale.range()[0]});
                    var ticks = yScale.ticks();
                    for(var t = 0; t < ticks.length; t++){

                        var pos = { x: xScale.range()[0], y: yScale(ticks[t]) };
                        chartService.drawLine(context, pos, {  x: pos.x - tickSize, y: pos.y });
                        chartService.drawText(context, ticks[t], {  x: pos.x - tickSize - 2, y: pos.y }, "12px Arial", "#666", "end", "middle");
                    }

                    // x-axis
                    chartService.drawLine(context, origin, {x: xScale.range()[1], y: yScale.range()[1]});

                    var tickCount = null;
                    if (chartType == "time")
                        tickCount = 8;
                    ticks = xScale.ticks(tickCount);
                    for(var t = 0; t < ticks.length; t++){

                        var pos = { x: xScale(ticks[t]), y: yScale.range()[1] };
                        chartService.drawLine(context, pos, {  x: pos.x, y: pos.y + tickSize });

                        if (chartType == 'time'){
                            var date = new Date(ticks[t]*1000);
                            var dateText = date.toLocaleDateString('en-US', {timeZone: "UTC", year: "numeric", month: "2-digit", day: "2-digit"});
                            var timeText = date.toLocaleTimeString('en-US', {timeZone: "UTC", hour12: false, hour: "2-digit", minute: "2-digit", second: "2-digit"});
                            chartService.drawText(context, dateText, {  x: pos.x, y: pos.y + tickSize + 2 }, "12px Arial", "#666", "center", "top");
                            chartService.drawText(context, timeText, {  x: pos.x, y: pos.y + tickSize + 14 }, "12px Arial", "#666", "center", "top");
                        }
                        else{
                            chartService.drawText(context, ticks[t], {  x: pos.x, y: pos.y + tickSize + 2 }, "12px Arial", "#666", "center", "top");
                        }
                    }

                    isRendered = true;
                }

                /**
                 *
                 */
                function setCanvasSize(){
                    // capture the parent's width and height
                    width = element.width();
                    height = element.height();
                    canvas.attr("width", width).attr("height", height);
                }

                function setOptions(){

                    if (!data) return;

                    // capture the chart type and check whether the index is a datetime
                    if (options.type == 'line' && data[featureIndex][0] > 946684800){
                        chartType = 'time';
                    }
                    else{
                        chartType = options.type;
                    }

                    var yMin = 0,
                        yMax = 1,
                        xMin = 0,
                        xMax = 1;

                    if (!options.scaled){
                        yMin = options.y.minActual || options.y.min || 0;
                        yMax = options.y.maxActual || options.y.max || 1;
                    }

                    // capture the chart boundaries
                    if (chartType == 'time'){

                        var timeSeries = data[featureIndex];
                        xMin = timeSeries[0];
                        xMax = timeSeries[timeSeries.length - 1];
                    }
                    else if (!options.scaled || chartType == 'line'){

                        xMin = options.x.minActual || options.x.min || 0;
                        xMax = options.x.maxActual || options.x.max || 1;
                    }

                    // create the chart scales
                    xScale = chartService.getXScale(chartType)
                        .range([margin.left, width - margin.right])
                        .domain([xMin, xMax])
                        .nice();

                    yScale = chartService.getYScale(chartType)
                        .range([margin.top, height - margin.bottom])
                        .domain([yMax, yMin])
                        .nice();

                    // setup zoom
                    canvas.call(d3.behavior.zoom()
                        .x(xScale)
                        .on("zoom", draw));
                }

                /**
                 * Setup the publicly exposed chart methods
                 */
                $scope.internalMethods = $scope.methods || {};

                $scope.internalMethods.render = function(chartOptions, chartData){

                    options = chartOptions;

                    if (chartData != null)
                        data = chartData;

                    setOptions();
                    draw();
                };

                // todo: handle window resize

                // initialize
                setCanvasSize();
            }
        }
    }])

    .directive('statisticsHistogram', ['chartService', 'colorService', function(chartService, colorService) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                values: "="
            },
            link: function ($scope, element, attrs) {

                function draw() {

                    var margin = {top: 10, right: 10, bottom: 10, left: 10};
                    var width = 180;
                    var height = 180;

                    var data = d3.layout.histogram().frequency(false)($scope.values);

                    var maxY = d3.max(data, function(d) { return d.y; });

                    var x = d3.scale.ordinal()
                        .domain(data)
                        .rangeBands([0, width], 0.1);

                    var y = d3.scale.linear()
                        .domain([0, maxY])
                        .range([0, height]);

                    var svg = d3.select(element[0])
                        .append("svg:svg")
                            .attr("width", width + margin.left + margin.right)
                            .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                    svg.selectAll("rect")
                        .data(data)
                        .enter()
                        .append("svg:rect")
                            .attr("x", x)
                            .attr("y", function(d) { return height-y(d.y); })
                            .attr("width", x.rangeBand())
                            .attr("height", function(d) { return y(d.y); })
                            .attr("fill", "steelblue")
                            .attr("shape-rendering", "crispEdges");
                }

                draw();
            }
        }
    }])

    .directive('statisticsBarChart', ['chartService', 'colorService', function(chartService, colorService) {

        return {
            restrict: 'E',
            replace: true,
            scope: {
                values: "="
            },
            link: function ($scope, element, attrs) {

                function draw() {

                    var data = $scope.values.histogram;

                    var margin = { top: 10, right: 10, bottom: 10, left: 10 };
                    var width = 200 - margin.left - margin.right;
                    var height = 200 - margin.top - margin.bottom;

                    var x = d3.scale.ordinal()
                        .rangeRoundBands([0, width], .1)
                        .domain(data.map(function(d) { return d.x; }));

                    var y = d3.scale.linear()
                        .range([height, 0])
                        .domain([0, d3.max(data, function(d) { return d.y; })]);

                    var svg = d3.select(element[0])
                        .append("svg")
                            .attr("width", width + margin.left + margin.right)
                            .attr("height", height + margin.top + margin.bottom)
                        .append("g")
                            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                    svg.selectAll(".bar")
                        .data(data)
                        .enter()
                        .append("rect")
                            .attr("class", "bar")
                            .attr("x", function(d) { return x(d.x); })
                            .attr("width", x.rangeBand())
                            .attr("y", function(d) { return y(d.y); })
                            .attr("height", function(d) { return height - y(d.y); });
                }

                draw();
            }
        }
    }]);

