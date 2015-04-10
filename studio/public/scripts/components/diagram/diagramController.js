'use strict';

diagramApp.controller('libraryModalController',
    ['$scope', '$modalInstance', 'nodes', 'getConfigBlock', 'loadSources',
        function($scope, $modalInstance, nodes, getConfigBlock, loadSources) {

        $scope.nodes = nodes;

        $scope.getConfigBlock = getConfigBlock;

        $scope.loadSources = loadSources;

        $scope.savable = false;

        $scope.save = function (block) {

            $modalInstance.close(block);
        };

        $scope.cancel = function () {

            $modalInstance.dismiss('cancel');
        };
        }])
    .controller('blockConfigController',
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
    ]);

