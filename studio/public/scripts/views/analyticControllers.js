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


    .controller('blockGroupController', ['$scope', '$element', 'position', 'close',
        function($scope, $element, position, close){

            $scope.position = position;

            $scope.close = function(transitionDelay){

                close(null, transitionDelay);
            };

            $scope.save = function(transitionDelay){

                close(null, transitionDelay);
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

                $scope.rendering = true;

                diagramService.getChartData($scope.block.uniqueName()).then(

                    function (data) {

                        $scope.chartData = data;

                        $scope.rendering = false;
                    },
                    function (code) {

                        $scope.rendering = false;
                    }
                );

            };

            $scope.save = function(transitionDelay) {

                close(null, transitionDelay);
            };

            $scope.setActiveIndex = function(index) {
                $scope.activeIndex = index;
            };

            /*  take the raw set of features and prepare for display
             */
            var prepare = function(features) {

                // get list of keys
                var keys = Object.keys(features);
                // create a set of colors for the list of features
                var colors = d3.scale.category20().domain(keys);

                // for each feature - created a selected flag and a color
                for (var i = 0; i < keys.length; i++){
                    var feature = features[keys[i]];
                    feature.selected = true;
                    feature.color = colors(keys[i]);
                }

                return features;
            };
        }
    ]);