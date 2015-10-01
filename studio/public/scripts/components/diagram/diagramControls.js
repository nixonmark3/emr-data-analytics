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
                    evaluating: "=",
                    command: "="
                },
                link: function($scope, element, attrs) {

                    // initialize scope variables
                    $scope.isRotated = true;

                    // initialize orbit position variables
                    var centerOffset = ($scope.centerRadius - $scope.orbitRadius),
                        angle = Math.sin(Math.PI / 4),
                        swOffset = angle * ($scope.offsetRadius - (angle / $scope.orbitRadius)) + centerOffset;

                    var initialPos = { x: centerOffset, y: centerOffset},
                        westPos = { x: $scope.offsetRadius + centerOffset, y: centerOffset},
                        southPos = { x: centerOffset, y: $scope.offsetRadius + centerOffset },
                        southwestPos = { x: swOffset, y: swOffset };

                    //
                    $scope.$watch("evaluating", function(){

                        toggle();
                    });

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

                    function init(){
                        $scope.initializing = true;

                        // initialize the orbit buttons to collapsed
                        collapse();
                        // pause and expand the orbit buttons
                        $timeout(function(){

                            expand();
                            $scope.initializing = false;
                            toggle();
                        }, 500);
                    }

                    $scope.onClick = function(evt, id){

                        if ($scope.command){

                            $scope.command(id);
                        }

                        evt.stopPropagation();
                        evt.preventDefault();
                    };

                    function toggle(){

                        if ($scope.initializing)
                            return;

                        if ($scope.evaluating)
                            collapse();
                        else
                            expand();
                    }

                    init();
                }
            }
    }]);