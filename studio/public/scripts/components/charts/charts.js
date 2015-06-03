'use strict';

angular.module('emr.ui.charts', [])

    .directive('timeSeriesChart', ['$window', function($window){

        return {
            restrict: 'E',
            replace: false,
            scope: {
                chartData: "="
            },
            link: function($scope, element, attrs){

                // setup constant variables
                var margin = { top: 20, right: 20, bottom: 30, left: 50 };

                // create svg
                var svg = d3.select(element[0])
                    .append("svg")
                        .style('width', '100%')
                        .style('height', '100%')
                    .append("g")
                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

                angular.element($window).bind("resize", function () {

                    render();
                });

                $scope.$watch("chartData", function () {

                    render();
                });

                var render = function(){

                    if (!$scope.chartData) return;

                    console.log("rendering time series chart.");

                    // remove all previous items before render
                    svg.selectAll('*').remove();

                    // setup variables
                    var width = element.width() - margin.left - margin.right,
                        height = element.height() - margin.top - margin.bottom;

                    // create chart scales and axes
                    var color = d3.scale.category20(),

                        xScale = d3.time.scale()
                            .range([0, width]),

                        yScale = d3.scale.linear()
                            .range([height, 0]),

                        xAxis = d3.svg.axis()
                            .scale(xScale)
                            .orient("bottom"),

                        yAxis = d3.svg.axis()
                            .scale(yScale)
                            .orient("left");

                    var line = d3.svg.line()
                        .interpolate("basis")
                        .x(function(d) { return xScale(d.date); })
                        .y(function(d) { return yScale(d.value); });

                    color.domain(d3.keys($scope.chartData));

                    var features = color.domain().map(function(name){
                        return {
                            name: name,
                            values: $scope.chartData[name].sort(function(a, b){
                                return a.date - b.date;
                            }).map(function(d) {
                                return { date: new Date(d.date*1000), value: d.value }
                            })
                        }
                    });

                    xScale.domain([
                        d3.min(features, function(d) { return d3.min(d.values, function(p) { return p.date; }); }),
                        d3.max(features, function(d) { return d3.max(d.values, function(p) { return p.date; }); })
                    ]);

                    yScale.domain([
                        d3.min(features, function(d) { return d3.min(d.values, function(p) { return p.value; }); }),
                        d3.max(features, function(d) { return d3.max(d.values, function(p) { return p.value; }); })
                    ]);

                    svg.append("g")
                        .attr("class", "x axis")
                        .attr("transform", "translate(0," + height + ")")
                        .call(xAxis);

                    svg.append("g")
                        .attr("class", "y axis")
                        .call(yAxis);

                    var feature = svg.selectAll(".feature")
                        .data(features)
                        .enter().append("g")
                        .attr("class", "feature");

                    feature.append("path")
                        .attr("class", "line")
                        .attr("d", function(d) { return line(d.values); })
                        .style("stroke", function(d) { return color(d.name); });
                };

                render();
            }
        }
    }]);