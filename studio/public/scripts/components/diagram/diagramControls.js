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
                    centerIcon: "@",
                    orbit1Icon: "@",
                    orbit2Icon: "@",
                    orbit3Icon: "@",
                    state: "=",
                    command: "="
                },
                link: function($scope, element, attrs) {

                    // initialize scope variables
                    $scope.isCollapsed = false;

                    // initialize orbit position variables
                    var centerOffset = ($scope.centerRadius - $scope.orbitRadius),
                        angle = Math.sin(Math.PI / 4),
                        swOffset = angle * ($scope.offsetRadius - (angle / $scope.orbitRadius)) + centerOffset;

                    var initialPos = { x: centerOffset, y: centerOffset},
                        westPos = { x: $scope.offsetRadius + centerOffset, y: centerOffset},
                        southPos = { x: centerOffset, y: $scope.offsetRadius + centerOffset },
                        southwestPos = { x: swOffset, y: swOffset };

                    // create an enumeration that defines this object's set of states
                    var controlStates = {
                        idle: 0,
                        starting: 1,
                        evaluating: 2,
                        stopping: 3
                    };

                    // watch the state
                    $scope.$watch("state", function(){
                        toggle();
                    });

                    /**
                     * collapse the control
                     */
                    function collapse(){
                        if($scope.isCollapsed)
                            return;

                        $scope.isCollapsed = true;
                        $scope.orbitWest = initialPos;
                        $scope.orbitSouth = initialPos;
                        $scope.orbitSouthwest = initialPos;
                    }

                    /**
                     * Expand the control
                     */
                    function expand(){
                        if(!$scope.isCollapsed)
                            return;

                        $scope.isCollapsed = false;
                        $scope.orbitWest = westPos;
                        $scope.orbitSouth = southPos;
                        $scope.orbitSouthwest = southwestPos;
                    }

                    /**
                     * initialize the control
                     */
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

                    /**
                     * on button click - send command to parent
                     * @param evt
                     * @param id
                     */
                    $scope.onClick = function(evt, id){

                        if ($scope.command)
                            $scope.command(id);

                        evt.stopPropagation();
                        evt.preventDefault();
                    };

                    /**
                     * on state change, toggle the control
                     */
                    function toggle(){

                        if ($scope.initializing)
                            return;

                        switch($scope.state){
                            case controlStates.idle:
                                expand();
                                break;
                            case controlStates.starting:
                            case controlStates.evaluating:
                            case controlStates.stopping:
                                collapse();
                                break;
                        }
                    }

                    init();
                }
            }
    }]);