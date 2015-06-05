'use strict';

angular.module('emr.ui.charts', [])

    .directive('timeSeriesChartCanvas', ['$window', function($window){

        return {
            restrict: 'E',
            replace: false,
            scope: {
                chartData: "="
            },
            link: function($scope, element, attrs){

                // setup constant variables
                var margin = { top: 20, right: 20, bottom: 30, left: 50 };

                // create canvas element
                var canvas = d3.select(element[0]).append("canvas");
                var context = canvas.node().getContext('2d');

                $scope.$watch("chartData", function () {

                    render();
                });

                var render = function(){

                    if (!$scope.chartData) return;

                    // setup variables
                    var width = element.width(),
                        height = element.height();

                    canvas.attr("width", width).attr("height", height);

                    // create chart scales and axes
                    var color = d3.scale.category20(),

                        xScale = d3.time.scale()
                            .range([margin.left, width-margin.right]),

                        yScale = d3.scale.linear()
                            .range([height-margin.top, margin.bottom]);

                    var labels = $scope.chartData[0];
                    var dates = $scope.chartData[1];
                    var features = $scope.chartData.filter(function(value, index){ return (index > 1); });

                    color.domain(labels);

                    xScale.domain([dates[0], dates[dates.length - 1]]);

                    var yMin = d3.min(features.map(function(feature){ return d3.min(feature); }));
                    var yMax = d3.max(features.map(function(feature){ return d3.max(feature); }));
                    yScale.domain([yMin, yMax]);

                    // setup zoom
                    canvas.call(d3.behavior.zoom()
                        .x(xScale)
                        .scaleExtent([1, 1000])
                        .on("zoom", zoom));

                    draw();

                    function zoom(){
                        context.clearRect(0, 0, width, height);
                        draw();
                    }

                    function draw(){

                        features.forEach(function(feature, index){

                            var i = 0;
                            var n = feature.length;

                            context.strokeStyle = color(labels[index]);
                            context.lineWidth = 1;
                            context.lineCap = "round";
                            context.beginPath();
                            context.moveTo(xScale(dates[i]), yScale(feature[i]));
                            while(++i < n){

                                context.lineTo(xScale(dates[i]), yScale(feature[i]));
                            }
                            context.stroke();
                        });
                    }
                };

                render();
            }
        }
    }]);