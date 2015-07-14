'use strict';

analyticsApp

    .controller('blockDataController',
    ['$scope', '$element', '$window', '$timeout', 'diagramService', 'block', 'config', 'position', 'close',
        function($scope, $element, $window, $timeout, diagramService, block, config, position, close){

            $scope.position = position;
            $scope.block = block.data;
            $scope.config = config;

            /*// calculate transitions for popup destination
            var transX = ($window.innerWidth / 2 - position.width / 2) - position.x;
            var transY = ($window.innerHeight / 2 - position.height / 2) - position.y;
            var transform = "translate(" + transX + "px," + transY + "px) scale(1)";

            $timeout(
                function(){
                    $element.css({ "-webkit-transform": transform });
                },
                30);*/

            $scope.close = function(){

                close(null, 500);

                $element.css({
                    "-webkit-transform": "translate(0, 0) scale(0.01)"
                });
            };

            $scope.getBlockData = function(type, key, success){

                switch(type){
                    case "Pages":
                        diagramService.availableBlockResults(key).then(
                            function(data){
                                success(data);
                            },
                            function (code) {
                                console.log(code); // TODO show exception
                            }
                        );
                        break;
                    case "Statistics":
                        diagramService.blockStatistics(key).then(
                            function(data){
                                success(data);
                            },
                            function (code) {
                                console.log(code); // TODO show exception
                            }
                        );
                        break;
                    case "Plot":
                        diagramService.blockPlot(key).then(
                            function(data){
                                success(data);
                            },
                            function (code) {
                                console.log(code); // TODO show exception
                            }
                        );
                        break;
                    case "Results":
                        diagramService.blockOutputResults(key).then(
                            function(data){
                                success(data);
                            },
                            function (code) {
                                console.log(code); // TODO show exception
                            }
                        );
                        break;
                }
            };
        }
    ])

    .controller('blockGroupController', ['$scope', '$element', '$timeout', 'diagramService', 'position', 'diagram', 'close',
        function($scope, $element, $timeout, diagramService, position, diagram, close){

            $scope.loading = true;
            $scope.position = position;

            // generate the list of the selected blocks by unique name
            var blocks = diagram.getSelectedBlocks().map(function(block){ return block.id() });

            // package up the group request
            var request = {
                name: diagram.generateBlockName("Group"), // create a unique for the new group
                diagram: diagram.data,
                blocks: blocks
            };

            $timeout(function(){
                diagramService.group(request).then(

                    function (data) {

                        // reference the resulting diagram
                        $scope.diagram = new viewmodels.diagramViewModel(data);

                        // reference the nested diagram
                        $scope.nestedDiagram = $scope.diagram.findDiagram(request.name);

                        $scope.loading = false;
                    },
                    function (code) {

                        $scope.loading = false;
                    }
                );
            }, 600);

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.diagram, transitionDelay);
            };
        }
    ])

    .controller('exploreController', ['$scope', '$element', '$timeout', '$q', '$animate', 'diagramService', 'colorService', 'block', 'config', 'position', 'close',
        function($scope, $element, $timeout, $q, $animate, diagramService, colorService, block, config, position, close){

            $scope.block = block;
            $scope.config = config;
            $scope.position = position;
            $scope.loading = true;
            $scope.rendering = false;
            $scope.activeIndex = 0;
            $scope.chartMethods = {};
            $scope.hideChartMenus = false;


            // load the set of features after the modal animation has completed
            $timeout(function() {

                diagramService.getFeatures($scope.block.id()).then(

                    function (data) {

                        $scope.features = data;
                        $scope.loading = false;
                    },
                    function (code) {

                        $scope.loading = false;
                    }
                );
            }, 600);

            // initialize the chart object
            $scope.chartOptions = {
                type: "line",
                colorSet: "default",
                x: { min: null, max: null },
                y: { min: null, max: null },
                series: []
            };

            // initialize new series object
            $scope.newSeries = { x: null, y: null, flipToFront: false, flipToBack: false };

            /* general methods */

            $scope.close = function(transitionDelay) {

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay) {

                close(null, transitionDelay);
            };

            $scope.setActiveIndex = function(index) {

                $scope.activeIndex = index;
                $scope.hideChartMenus = false;
            };

            /* chart methods */

            $scope.showClose = function(series) {

                series.flipToBack = true;
                series.flipToFront = false;
            };

            $scope.showCheck = function(series) {

                series.flipToBack = false;
                series.flipToFront = true;
            };

            $scope.collapseChartMenusClick = function() {

                $scope.hideChartMenus = true;
            };

            $scope.addAllSeries = function() {

                if ($scope.chartOptions.type == 'line') {

                    resetSeries();

                    var currentConfiguredSeries = getCurrentConfiguredSeries();

                    for (var featureIndex in $scope.features) {

                        if (featureIndex > 0) {

                            var seriesName = $scope.features[featureIndex].column;

                            if (currentConfiguredSeries.indexOf(seriesName) == -1) {

                                var seriesToAdd = angular.copy($scope.newSeries);

                                seriesToAdd.y = $scope.features[featureIndex].column;

                                $scope.chartOptions.series.push(seriesToAdd);
                            }
                        }
                    }

                    updateChartBounds();

                    $scope.render();
                }
            };

            var getCurrentConfiguredSeries = function() {

                var configuredSeries = [];

                for (var series in $scope.chartOptions.series) {

                    configuredSeries.push($scope.chartOptions.series[series].y);
                }

                return configuredSeries;

            };

            $scope.removeAllSeries = function() {

                $scope.chartOptions.series.length = 0;

                updateChartBounds();

                $scope.render();
            };

            $scope.removeSeries = function(series) {

                $scope.chartOptions.series.splice($scope.chartOptions.series.indexOf(series), 1);

                updateChartBounds();

                $scope.render();
            };

            $scope.addSeries = function() {

                if ($scope.newSeries.y == null) return;

                if ($scope.hasXCoordinate() && $scope.newSeries.x == null) return;

                var seriesToAdd = angular.copy($scope.newSeries);

                $scope.chartOptions.series.push(seriesToAdd);

                updateChartBounds();

                resetSeries();

                $scope.render();
            };

            // retrieve a color by configured color set and index
            $scope.getColor = function(index) {

                return colorService.getColor($scope.chartOptions.colorSet, index);
            };

            // determines whether currently selected chart type has a configurable x coordinate
            $scope.hasXCoordinate = function() {

                return ($scope.chartOptions.type == 'scatter');
            };

            $scope.onPage = function(){

                var deferred = $q.defer();

                diagramService.getFeatureGridData($scope.block.id()).then(
                    function (data) {

                        deferred.resolve(data);
                    },
                    function (code) {

                        deferred.reject(code);
                    }
                );

                return deferred.promise;
            };

            // render the chart
            $scope.render = function() {

                $scope.rendering = true;

                // assemble a distinct list of features
                // todo: to maintain a dictionary of features as series and added and removed
                var features = [];
                for(var i = 0; i < $scope.chartOptions.series.length; i++){

                    var series = $scope.chartOptions.series[i];
                    if (series.x != null && features.indexOf(series.x) == -1)
                        features.push(series.x);
                    if (features.indexOf(series.y) == -1)
                        features.push(series.y);
                }

                diagramService.getChartData($scope.block.id(), features).then(
                    function (data) {

                        $scope.chartMethods.render($scope.chartOptions, data);
                        $scope.rendering = false;
                    },
                    function (code) {

                        $scope.rendering = false;
                    }
                );
            };

            function resetSeries() {

                $scope.newSeries.y = null;
                $scope.newSeries.x = null;
                $scope.newSeries.flipToFront = false;
                $scope.newSeries.flipToBack = false;
            }

            function updateChartBounds() {

                var count = 0;
                var xMin = 0;
                var xMax = 0;
                var yMin = 0;
                var yMax = 0;

                for (var seriesIndex in $scope.chartOptions.series) {

                    var yFeature = getFeatureStatistics($scope.chartOptions.series[seriesIndex].y);
                    var xFeature = ($scope.chartOptions.series[seriesIndex].x) ? getFeatureStatistics($scope.chartOptions.series[seriesIndex].x) : null;

                    if (count === 0) {

                        count = 1;

                        var yMin = yFeature.min;
                        var yMax = yFeature.max;

                        if (xFeature != null) {

                            var xMin = xFeature.min;
                            var xMax = xFeature.max;
                        }
                        else {

                            var xMin = 0;
                            var xMax = yFeature.count;
                        }
                    }
                    else {

                        if (yFeature.min < yMin) {

                            yMin = yFeature.min;
                        }

                        if (yFeature.max > yMax) {

                            yMax = yFeature.max;
                        }

                        if (xFeature != null) {

                            if (xFeature.min < xMin) {

                                xMin = xFeature.min;
                            }

                            if (xFeature.max > xMax) {

                                xMax = xFeature.max;
                            }
                        }
                        else {

                            if (xMin > 0) {

                                xMin = 0;
                            }

                            if (yFeature.count > xMax) {

                                xMax = yFeature.count;
                            }
                        }
                    }
                }

                $scope.chartOptions.y.min = yMin;
                $scope.chartOptions.y.max = yMax;
                $scope.chartOptions.x.min = xMin;
                $scope.chartOptions.x.max = xMax;
            }

            function getFeatureStatistics(selectedFeature) {

                var statistics = null;

                $scope.features.forEach(function (feature) {

                    if (feature.column === selectedFeature) {

                        statistics =  feature.statistics;
                    }
                });

                return statistics;
            }

            $scope.updateChartType = function(){

                $scope.chartOptions.x = { min: null, max: null };
                $scope.chartOptions.y = { min: null, max: null };
                $scope.chartOptions.series = [];

                resetSeries();
            };
        }
    ])

    .controller('libraryBrowserController', ['$scope', '$element', 'nodes', 'onDrag', 'onDrop', 'close',
        function($scope, $element, nodes, onDrag, onDrop, close) {

            $scope.nodes = nodes;

            $scope.onDrag = onDrag;

            $scope.onDrop = onDrop;

            $scope.close = function(){

                close();
            };

            this.close = $scope.close;
        }
    ]);