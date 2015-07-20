'use strict';

angular.module('emr.ui.controls', [])

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

    }]);