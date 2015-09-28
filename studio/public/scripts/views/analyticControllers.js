'use strict';

analyticsApp

    .controller('blockDataController', ['$scope', '$element', '$window', '$timeout', 'diagramService', 'block', 'config', 'position', 'close',
        function($scope, $element, $window, $timeout, diagramService, block, config, position, close){

            $scope.position = position;
            $scope.block = block.data;
            $scope.config = config;

            $scope.close = function(transitionDuration){

                close(null, transitionDuration);
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

    .controller('debugController', ['$scope', '$element', '$timeout', 'diagramService', 'data', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, data, config, position, close){

            $scope.config = config;
            $scope.position = position;

            diagramService.compile(data).then(
                function (source) {
                    $scope.editor = { data: source };
                },
                function (code) {
                    console.log(code); // TODO show exception
                }
            );

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };
        }
    ])

    .controller('blockConfigEditorController', ['$scope', '$element', '$timeout', 'diagramService', 'data', 'config', 'position', 'close',
        function($scope, $element, $timeout, diagramService, data, config, position, close){

            $scope.config = config;
            $scope.position = position;
            $scope.editor = { data: data };

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.editor.data, transitionDelay);
            };
        }
    ])

    .controller('editorController', ['$scope', '$element', '$timeout', 'config', 'position', 'close',
        function($scope, $element, $timeout, config, position, close){

            $scope.config = config;
            $scope.position = position;

            // todo: temporarily hardcode default json
            $scope.editor = {
                data: JSON.stringify({
                topic: "SIM",
                streamingSource: {
                    pollingSourceType: "Simulated",
                    url: "http://localhost",
                    frequency: 1,
                    keys: [
                        "IN7OUT1MODEL/TAG1.CV",
                        "IN7OUT1MODEL/TAG2.CV",
                        "IN7OUT1MODEL/TAG3.CV",
                        "IN7OUT1MODEL/TAG4.CV",
                        "IN7OUT1MODEL/TAG5.CV",
                        "IN7OUT1MODEL/TAG6.CV",
                        "IN7OUT1MODEL/TAG7.CV"
                    ]}
                }, null, '\t')
            };

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close($scope.editor.data, transitionDelay);
            };
        }
    ])

    .controller('exploreController', ['$scope', '$element', '$timeout', '$q', '$animate', 'diagramService', 'colorService', 'block', 'config', 'position', 'close',
        function($scope, $element, $timeout, $q, $animate, diagramService, colorService, block, config, position, close){

            $scope.block = block;
            $scope.config = config;
            $scope.position = position;
            $scope.loading = true;
            $scope.fetching = false;
            $scope.rendering = false;
            $scope.activeIndex = 0;
            $scope.chartMethods = {};
            $scope.hideChartMenus = false;
            $scope.rangeSliderDisabled = false;

            $scope.config.subTitle = "Explore";

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
            }, 400);

            // initialize the chart object
            $scope.chartOptions = {
                type: "line",
                scaled: false,
                override: false, 
                colorSet: "default",
                x: { min: null, max: null, minActual: null, maxActual: null },
                y: { min: null, max: null, minActual: null, maxActual: null },
                series: []
            };

            // initialize new series object
            $scope.newSeries = {
                x: {
                    name: null, min: null, max: null
                },
                y: {
                    name: null, min: null, max: null
                },
                flipToFront: false,
                flipToBack: false
            };

            $scope.$watch(
                function() {
                    return $scope.chartOptions.y.minActual;
                },
                function() {
                    if($scope.loading || $scope.fetching) {

                        console.log("fetching...");
                        return;
                    }

                    if ($scope.chartOptions.y.minActual != null)
                        $scope.render();
                }
            );

            $scope.$watch(
                function() {
                    return $scope.chartOptions.y.maxActual;
                },
                function() {
                    if($scope.loading || $scope.fetching) {

                        console.log("fetching...");
                        return;
                    }

                    if ($scope.chartOptions.y.maxActual != null)
                        $scope.render();
                }
            );

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

                                var feature = $scope.features[featureIndex];
                                seriesToAdd.y.name = feature.column;
                                seriesToAdd.y.min = feature.min;
                                seriesToAdd.y.max = feature.max;

                                $scope.chartOptions.series.push(seriesToAdd);
                            }
                        }
                    }

                    updateChartBounds();

                    $scope.fetchData();
                }
            };

            var getCurrentConfiguredSeries = function() {

                var configuredSeries = [];

                for (var series in $scope.chartOptions.series) {

                    configuredSeries.push($scope.chartOptions.series[series].y.name);
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

                if ($scope.newSeries.y.name == null) return;

                if ($scope.hasXCoordinate() && $scope.newSeries.x.name == null) return;

                var seriesToAdd = angular.copy($scope.newSeries);

                $scope.chartOptions.series.push(seriesToAdd);

                updateChartBounds();

                resetSeries();

                $scope.fetchData();
            };

            /**
             *  Configure
             */
            $scope.selectFeature = function(type){

                var dimension;
                if (type == 'y')
                    dimension = $scope.newSeries.y;
                else if (type == 'x')
                    dimension = $scope.newSeries.x;

                var feature = getFeatureStatistics(dimension.name);
                dimension.min = feature.min;
                dimension.max = feature.max;
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

            $scope.toggleScale = function(){

                if($scope.chartOptions.scaled)
                    $scope.rangeSliderDisabled = true;
                else
                    $scope.rangeSliderDisabled = false;

                $scope.render();
            };

            // fetch data and render the chart
            $scope.fetchData = function() {

                $scope.fetching = true;

                // assemble a distinct list of features
                // todo: to maintain a dictionary of features as series and added and removed
                var features = [];
                for(var i = 0; i < $scope.chartOptions.series.length; i++){

                    var series = $scope.chartOptions.series[i];
                    if (series.x.name != null && features.indexOf(series.x.name) == -1)
                        features.push(series.x.name);
                    if (features.indexOf(series.y.name) == -1)
                        features.push(series.y.name);
                }

                diagramService.getChartData($scope.block.id(), features).then(
                    function (data) {

                        $scope.fetching = false;

                        $scope.render(data);
                    },
                    function (code) {

                        $scope.rendering = false;
                    }
                );
            };

            $scope.render = function(data){

                $scope.rendering = true;
                $scope.chartMethods.render($scope.chartOptions, data);
                $scope.rendering = false;
            };

            function resetSeries() {

                $scope.newSeries.y = {
                    name: null,
                    min: null,
                    max: null
                };
                $scope.newSeries.x = {
                    name: null,
                    min: null,
                    max: null
                };
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

                    var yFeature = getFeatureStatistics($scope.chartOptions.series[seriesIndex].y.name);
                    var xFeature = ($scope.chartOptions.series[seriesIndex].x.name) ? getFeatureStatistics($scope.chartOptions.series[seriesIndex].x.name) : null;

                    var yFeatureMin = Number(yFeature.min);
                    var yFeatureMax = Number(yFeature.max);

                    if (xFeature != null) {

                        var xFeatureMin = Number(xFeature.min);
                        var xFeatureMax = Number(xFeature.max);
                    }

                    var yFeatureCount = Number(yFeature.count);

                    if (count === 0) {

                        count = 1;

                        yMin = yFeatureMin;
                        yMax = yFeatureMax;

                        if (xFeature != null) {

                            xMin = xFeatureMin;
                            xMax = xFeatureMax;
                        }
                        else {

                            xMin = 0;
                            xMax = yFeatureCount;
                        }
                    }
                    else {

                        if (yFeatureMin < yMin) {

                            yMin = yFeatureMin;
                        }

                        if (yFeatureMax > yMax) {

                            yMax = yFeatureMax;
                        }

                        if (xFeature != null) {

                            if (xFeatureMin < xMin) {

                                xMin = xFeatureMin;
                            }

                            if (xFeatureMax > xMax) {

                                xMax = xFeatureMax;
                            }
                        }
                        else {

                            if (xMin > 0) {

                                xMin = 0;
                            }

                            if (yFeatureCount > xMax) {

                                xMax = yFeatureCount;
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

                for (var i = 0; i < $scope.features.length; i++){

                    var feature = $scope.features[i];
                    if (feature.column === selectedFeature) {
                        statistics =  feature.statistics;
                        break;
                    }
                }

                return statistics;
            }

            $scope.updateChartType = function(){

                $scope.chartOptions.x = { min: null, max: null, minActual: null, maxActual: null };
                $scope.chartOptions.y = { min: null, max: null, minActual: null, maxActual: null };
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
    ])

    .controller('loadDataController', ['$scope', '$element', 'diagramService', 'config', 'position', 'close',
        function($scope, $element, diagramService, config, position, close){

            $scope.position = position;
            $scope.config = config;

            $scope.close = function(transitionDuration){

                close(null, transitionDuration);
            };
    }]);