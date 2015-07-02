'use strict';

analyticsApp

    .controller('blockDataController',
    ['$scope', '$element', '$window', '$timeout', 'diagramService', 'block', 'position', 'close',
        function($scope, $element, $window, $timeout, diagramService, block, position, close){

            $scope.position = position;
            $scope.block = block.data;

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

    .controller('exploreController', ['$scope', '$element', '$timeout', 'diagramService', 'colorService', 'block', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, colorService, block, config, position, close){

            $scope.block = block;
            $scope.config = config;
            $scope.position = position;
            $scope.loading = true;
            $scope.rendering = false;
            $scope.activeIndex = 0;
            $scope.chartMethods = {};

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
            $scope.newSeries = { x: null, y: null };

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

            $scope.addSeries = function(){

                // verify series data has been collected
                if ($scope.newSeries.y==null) return;
                if ($scope.hasXCoordinate() && $scope.newSeries.x==null) return;

                // update min and max values for each boundary
                updateChartBounds($scope.newSeries);

                // add the new series to the chart object
                $scope.chartOptions.series.push(angular.copy($scope.newSeries));

                resetSeries();

                // render the chart
                $scope.render();
            };

            // retrieve a color by configured color set and index
            $scope.getColor = function(index){
                return colorService.getColor($scope.chartOptions.colorSet, index);
            };

            // determines whether currently selected chart type has a configurable x coordinate
            $scope.hasXCoordinate = function(){
                return ($scope.chartOptions.type == 'scatter');
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

            function resetSeries(){
                // reset new series
                $scope.newSeries.y = null;
                $scope.newSeries.x = null;
            }

            // update the maximum and minimum chart bounds based on the specified feature
            function updateChartBounds(series){

                var yFeature = getFeatureStatistics(series.y);
                var xFeature = (series.x) ? getFeatureStatistics(series.x) : null;

                console.log(yFeature);

                // update y boundaries
                if ($scope.chartOptions.y.min==null || yFeature.min < $scope.chartOptions.y.min)
                    $scope.chartOptions.y.min = yFeature.min;
                if ($scope.chartOptions.y.max==null || yFeature.max > $scope.chartOptions.y.max)
                    $scope.chartOptions.y.max = yFeature.max;

                // update x boundaries
                if (xFeature != null){
                    if ($scope.chartOptions.x.min==null || xFeature.min < $scope.chartOptions.x.min)
                        $scope.chartOptions.x.min = xFeature.min;
                    if ($scope.chartOptions.x.max==null || xFeature.max > $scope.chartOptions.x.max)
                        $scope.chartOptions.x.max = xFeature.max;
                }
                else{
                    if ($scope.chartOptions.x.min==null || $scope.chartOptions.x.min > 0)
                        $scope.chartOptions.x.min = 0;
                    if ($scope.chartOptions.x.max==null || yFeature.count > $scope.chartOptions.x.max)
                        $scope.chartOptions.x.max = yFeature.count;
                }
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