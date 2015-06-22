'use strict';

diagramApp
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
    ]);

