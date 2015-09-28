'use strict';

diagramApp
    .directive('diagramControls', ['$timeout',
        function($timeout){

            return {
                restrict: 'E',
                replace: true,
                templateUrl: '/assets/scripts/components/diagram/diagramControls.html',
                scope: {
                    centerRadius: "=",
                    orbitRadius: "=",
                    offsetRadius: "=",
                    load: "="
                },
                link: function($scope, element, attrs) {

                    // initialize scope variables
                    $scope.isEvaluating = false;
                    $scope.isRotated = true;

                    // initialize orbit position variables
                    var centerOffset = ($scope.centerRadius - $scope.orbitRadius),
                        angle = Math.sin(Math.PI / 4),
                        swOffset = angle * ($scope.offsetRadius - (angle / $scope.orbitRadius)) + centerOffset;

                    var initialPos = { x: centerOffset, y: centerOffset},
                        westPos = { x: $scope.offsetRadius + centerOffset, y: centerOffset},
                        southPos = { x: centerOffset, y: $scope.offsetRadius + centerOffset },
                        southwestPos = { x: swOffset, y: swOffset };

                    // initialize the orbit buttons to collapsed
                    collapse();
                    // pause and expand the orbit buttons
                    $timeout(expand, 500);

                    function collapse(){
                        $scope.isRotated = true;
                        $scope.orbitWest = initialPos;
                        $scope.orbitSouth = initialPos;
                        $scope.orbitSouthwest = initialPos;
                    }

                    function expand(){
                        $scope.isRotated = false;
                        $scope.orbitWest = westPos;
                        $scope.orbitSouth = southPos;
                        $scope.orbitSouthwest = southwestPos;
                    }

                    $scope.onClick = function(id){
                        collapse();
                        $scope.isEvaluating = true;

                        $timeout(function(){
                                $scope.isEvaluating = false;
                                expand();

                                $scope.$$phase || $scope.$apply();
                            },
                            3000);
                    };

                    /**
                     * method bound to load data click event
                     */
                    $scope.onLoad = function(evt){

                        if ($scope.load){

                            $scope.load();
                        }

                        evt.stopPropagation();
                        evt.preventDefault();
                    };
                }
            }
    }]);