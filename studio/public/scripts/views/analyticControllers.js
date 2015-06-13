'use strict';

analyticsApp

    .controller('blockConfigController', ['$scope', '$element', 'block', 'position', 'loadSources', 'close',
        function($scope, $element, block, position, loadSources, close) {

            $scope.block = block;
            $scope.loadSources = loadSources;

            // calculate position based on block dimensions
            if ((position.x - position.width - 20) > 0) {         // left align
                $scope.alignLeft = true;
                position.x = position.x - position.width - 20;
            }
            else {                                                // right align
                $scope.alignLeft = false;
                position.x = position.x + block.w + 20;
            }

            $scope.position = position;

            $scope.cancel = function(){

                close();
            };

            $scope.save = function(){

                close(block);
            };

            this.cancel = $scope.close;

            this.save = $scope.save;
        }
    ])

    .controller('blockGroupController', ['$scope', '$element', '$timeout', 'diagramService', 'position', 'diagram', 'close',
        function($scope, $element, $timeout, diagramService, position, diagram, close){

            $scope.loading = true;
            $scope.position = position;

            // generate the list of the selected blocks by unique name
            var blocks = diagram.getSelectedBlocks().map(function(block){ return block.uniqueName() });

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

            // loading data after zoom animation has completed
            $timeout(function() {

                diagramService.getFeatures($scope.block.uniqueName()).then(

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

                    diagramService.getChartData($scope.block.uniqueName(), selectedFeatures.toString()).then(
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

            $scope.setActiveIndex = function(index) {
                $scope.activeIndex = index;
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
            }

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
    ]);