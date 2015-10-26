'use strict';

var controlsApp = angular.module('emr.ui.controls', [])

    .directive('rangeSlider', [function(){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/controls/rangeSlider.html',
            scope: {
                minLimit: "=",
                minValue: "=",
                maxLimit: "=",
                maxValue: "=",
                disabled: "="
            },
            link: function($scope, element, attrs){

                var min, max;

                $scope.minValue = $scope.minLimit;
                $scope.maxValue = $scope.maxLimit;
                $scope.minDirty = false;
                $scope.maxDirty = false;

                $scope.$watch(
                    function(){
                        return $scope.minLimit;
                    },
                    function(){

                        if ($scope.minLimit == null){
                            $scope.minValue = null;
                        }
                        else if (!$scope.minDirty || Number($scope.minValue) < Number($scope.minLimit)) {
                            $scope.minValue = $scope.minLimit;
                            $scope.minDirty = false;
                        }
                        else if ($scope.maxLimit != null && Number($scope.minValue) > Number($scope.maxLimit)){
                            $scope.minValue = $scope.maxLimit;
                        }

                        setStep();
                    }
                );

                $scope.$watch(
                    function(){
                        return $scope.maxLimit;
                    },
                    function(){

                        if ($scope.maxLimit == null){
                            $scope.maxValue = null;
                        }
                        else if (!$scope.maxDirty || Number($scope.maxValue) > Number($scope.maxLimit)) {
                            $scope.maxValue = $scope.maxLimit;
                            $scope.maxDirty = false;
                        }
                        else if ($scope.minLimit != null && Number($scope.maxValue) < Number($scope.minLimit)){
                            $scope.maxValue = $scope.minLimit;
                        }

                        setStep();
                    }
                );

                $scope.updateMin = function(){
                    $scope.minDirty = true;
                    if(Number($scope.minValue) > Number($scope.maxValue)){
                        $scope.maxValue = $scope.minValue;
                    }
                };

                $scope.updateMax = function(){
                    $scope.maxDirty = true;
                    if(Number($scope.maxValue) < Number($scope.minValue)){
                        $scope.minValue = $scope.maxValue;
                    }
                };

                function setStep(){

                    if ($scope.minLimit == null || $scope.maxLimit == null)
                        $scope.step = 1;
                    else {
                        min = Number($scope.minLimit);
                        max = Number($scope.maxLimit);
                        $scope.step = (max - min) / 100;
                    }
                }

                setStep();
            }
        }

    }])

    .directive('expand', ['$timeout', function($timeout){

        return {
            restrict: 'A',
            scope: {
                expand: "="
            },
            link: function($scope, element, attrs){

                var steps = 20, delay = 10, childHeight = 0;

                $scope.$watch("expand", toggle);

                // internal variables

                function expand(step, stepSize, start){
                    element.height((step + 1) * stepSize + start);

                    step += 1;
                    if (step < steps)
                        $timeout(function(){ expand(step, stepSize, start); }, delay);
                }

                function getChildHeight(){
                    var height = 0,
                        children = element.children();

                    for(var i = 0; i < children.length; i++)
                        height += children[i].clientHeight;

                    return height;
                }

                function init(){
                    childHeight = getChildHeight();
                    toggle();
                }

                function toggle(){

                    var start = element.height();
                    var end = ($scope.expand) ? childHeight : 0;

                    if (start != end){
                        var stepSize = (end - start) / steps;
                        expand(0, stepSize, start);
                    }
                }

                init();
            }
        }
    }]);