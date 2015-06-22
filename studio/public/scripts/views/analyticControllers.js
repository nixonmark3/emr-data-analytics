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

    .controller('chartsController', ['$scope', '$element', '$timeout', 'block', 'diagramService', 'position', 'close',
        function($scope, $element, $timeout, block, diagramService, position, close){

            $scope.block = block;
            $scope.loading = true;
            $scope.rendering = false;
            $scope.position = position;
            $scope.activeIndex = 0;
            $scope.chartType = 0;
            $scope.hideChartMenus = false;

            // loading data after zoom animation has completed
            $timeout(function() {

                diagramService.getFeatures($scope.block.id()).then(

                    function (data) {

                        $scope.features = prepare(data);

                        $scope.loading = false;
                    },
                    function (code) {

                        $scope.loading = false;
                    }
                );
            }, 600);

            $scope.close = function(transitionDelay) {

                close(null, transitionDelay);
            };

            $scope.render = function() {

                var selectedFeatures = getSelectedFeatures();

                if (selectedFeatures.length != 0) {

                    $scope.rendering = true;

                    diagramService.getChartData($scope.block.id(), selectedFeatures.toString()).then(
                        function (data) {

                            $scope.chartData = data;

                            $scope.rendering = false;
                        },
                        function (code) {

                            $scope.rendering = false;
                        }
                    );

                }
            };

            $scope.save = function(transitionDelay) {

                close(null, transitionDelay);
            };

            $scope.collapseChartMenusClick = function() {

                $scope.hideChartMenus = true;
            };

            $scope.setActiveIndex = function(index) {

                $scope.activeIndex = index;
                $scope.hideChartMenus = false;
            };

            $scope.featureSelectClick = function(feature) {

                feature.selected = !feature.selected;

                if (feature.selected) {

                    feature.flipToBack = false;
                    feature.flipToFront = true;
                }
                else {

                    feature.flipToBack = true;
                    feature.flipToFront = false;
                }
            };

            /*  take the raw set of features and prepare for display
             */
            var prepare = function(features) {

                // get list of keys
                var keys = Object.keys(features);
                // create a set of colors for the list of features
                var colors = d3.scale.category20().domain(keys);

                // for each feature - created a selected flag and a color
                for (var i = 0; i < keys.length; i++) {

                    var feature = features[keys[i]];
                    feature.selected = true;
                    feature.color = colors(keys[i]);
                    feature.showStatistics = false;
                    feature.min = feature.min.toFixed(5);
                    feature.mean = feature.mean.toFixed(5);
                    feature.std = feature.std.toFixed(5);
                    feature.max = feature.max.toFixed(5);
                    feature.twentyFive = feature.twentyFive.toFixed(5);
                    feature.fifty = feature.fifty.toFixed(5);
                    feature.seventyFive = feature.seventyFive.toFixed(5);
                    feature.flipToFront = false;
                    feature.flipToBack = false;
                }

                return features;
            };

            var getSelectedFeatures = function() {

                var selectedFeatures = [];

                for (var key in $scope.features) {

                    if ($scope.features[key].selected) {

                        selectedFeatures.push(key);
                    }
                }

                return selectedFeatures;
            }
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