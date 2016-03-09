'use strict';

var controlsApp = angular.module('emr.ui.controls', [])

    .directive('dateTimePicker', [function(){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/controls/dateTimePicker.html',
            scope: {
                header: "@",
                value: "="
            },
            link: function($scope, element, attrs){

                if ($scope.value === null)
                    $scope.date = new Date();
                else
                    $scope.date = new Date($scope.value);
                $scope.hours = pad($scope.date.getHours());
                $scope.minutes = pad($scope.date.getMinutes());

                $scope.onUpdate = function(field){

                    switch(field){
                        case "hours":
                            $scope.hours = pad(Number($scope.hours));
                            break;
                        case "minutes":
                            $scope.minutes = pad(Number($scope.minutes));
                            break;
                    }

                    $scope.date.setHours(Number($scope.hours), Number($scope.minutes));

                    // set value on update
                    // Format: 2016-01-10T14:55:00.000Z
                    $scope.value = $scope.date.getFullYear() + "-"
                        + pad($scope.date.getMonth() + 1) + "-" + pad($scope.date.getDate()) + "T"
                        + pad($scope.date.getHours()) + ":" + pad($scope.date.getMinutes()) + ":00.000Z";
                };

                $scope.onKey = function(field, evt){

                    var keyCommand = false;
                    if (evt.keyCode == 38) {
                        $scope.setNumber('increment', field);
                        keyCommand = true;
                    }
                    else if(evt.keyCode == 40) {
                        $scope.setNumber('decrement', field);
                        keyCommand = true;
                    }

                    if(keyCommand) {
                        evt.stopPropagation();
                        evt.preventDefault();
                    }
                };

                $scope.setNumber = function(direction, field){

                    var digit;
                    if (field === "hours"){
                        if (direction === 'increment') { // increment
                            digit = ($scope.hours == 23) ? 0 : ++$scope.hours;
                        }
                        else {
                            digit = ($scope.hours == 0) ? 23 : --$scope.hours;
                        }
                        $scope.hours = pad(digit);
                    }
                    else if (field === "minutes"){
                        if (direction === 'increment') { // increment
                            digit = ($scope.minutes == 59) ? 0 : ++$scope.minutes;
                        }
                        else {
                            digit = ($scope.minutes == 0) ? 59 : --$scope.minutes;
                        }
                        $scope.minutes = pad(digit);
                    }

                    $scope.onUpdate(field);
                };

                function pad(digit) {
                    return (digit >= 0 && digit < 10) ? "0" + digit : digit + "";
                }
            }
        }
    }])

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

    .directive('toggleSlider', [function(){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/controls/toggleSlider.html',
            scope: {
                value: "=",
                disabled: "="
            },
            link: function($scope, element, attrs){

                $scope.onToggle = function(){
                    if($scope.disabled)
                        return;
                    $scope.value = !$scope.value;
                };
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