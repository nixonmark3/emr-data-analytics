'use strict';

diagramApp.controller('blockConfigController',
    ['$scope', '$element', 'block', 'position', 'loadSources', 'close',
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

            $scope.close = function(){

                close(block);
            };

            this.close = $scope.close;
        }
    ])
    .controller('diagramConfigController',
    ['$scope', '$element', 'diagram', 'close',
        function($scope, $element, diagram, close) {

            $scope.diagram = diagram;

            $scope.close = function(){

                close(diagram);
            };

            this.close = $scope.close;
        }
    ])
    .controller('libraryController',
    ['$scope', '$element', 'nodes', 'getConfigBlock', 'loadSources', 'close',
        function($scope, $element, nodes, getConfigBlock, loadSources, close) {

            $scope.nodes = nodes;

            $scope.getConfigBlock = getConfigBlock;

            $scope.loadSources = loadSources;

            $scope.savable = false;

            $scope.save = function(block){

                close(block);
            };

            $scope.cancel = function(){

                close();
            };
        }
    ])
    .controller('libraryBrowserController',
    ['$scope', '$element', 'nodes', 'onDrag', 'onDrop', 'close',
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
    .controller('blockDataController',
    ['$scope', '$element', '$window', '$timeout', 'block', 'position', 'getBlockData', 'close',
        function($scope, $element, $window, $timeout, block, position, getBlockData, close){

            // center popup over block
            position.x = position.x - (position.width / 2 - block.width() / 2);
            position.y = position.y - (position.height / 2 - block.height() / 2);
            $scope.position = position;

            $scope.getBlockData = getBlockData;

            $scope.block = block.data;

            // calculate transitions for popup destination
            var transX = ($window.innerWidth / 2 - position.width / 2) - position.x;
            var transY = ($window.innerHeight / 2 - position.height / 2) - position.y;
            var transform = "translate(" + transX + "px," + transY + "px) scale(1)";

            $timeout(
                function(){
                    $element.css({ "-webkit-transform": transform });
                },
                30);

            $scope.close = function(){

                close(null, 500);

                $element.css({
                    "-webkit-transform": "translate(0, 0) scale(0.01)"
                });
            };

            this.close = $scope.close;
        }
    ]);

