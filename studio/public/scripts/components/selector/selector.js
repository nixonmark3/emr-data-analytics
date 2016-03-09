'use strict';

controlsApp.directive('selector', [function(){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/selector/selector.html',
            scope: {
                header: "=",
                values: "=",
                options: "="
            },
            link: function($scope, element, attrs){

                var input = element[0].querySelector("input"),
                    charWidth = 8;

                $scope.inputValue = "";
                $scope.inputWidth = charWidth;
                $scope.optionsVisible = false;
                $scope.isDisabled = false;
                $scope.filteredOptions = [];

                function clearValues(){
                    $scope.values.length = 0;
                }

                function filterOptions(){

                    var filtered = [];
                    for (var index = 0; index < $scope.options.length; index++) {
                        if (!isValue($scope.options[index])
                            && ($scope.inputValue === ""
                            || $scope.options[index].toLowerCase().indexOf($scope.inputValue.toLowerCase()) > -1))

                            filtered.push($scope.options[index]);
                    }

                    $scope.filteredOptions = filtered;
                }

                function isValue(value){

                    var index = $scope.values.length;
                    while(index--){
                        if($scope.values[index] === value)
                            return true;
                    }

                    return false;
                }

                $scope.addValue = function(value){
                    $scope.values.push(value);
                    filterOptions();
                };

                $scope.addValues = function(){

                    clearValues();
                    for (var index = 0; index < $scope.options.length; index++)
                        $scope.values.push($scope.options[index]);

                    $scope.filteredOptions = [];
                };

                $scope.cancelBlur = function(evt){
                    evt.stopPropagation();
                    evt.preventDefault();
                };

                $scope.onBlur = function(){
                    $scope.inputValue = "";
                    $scope.optionsVisible = false;
                };

                $scope.onChange = function() {

                    // set the textbox width
                    var tempWidth = ($scope.inputValue.length + 1) * charWidth;
                    var containerWidth = element.width() - 20;
                    $scope.inputWidth = (tempWidth < containerWidth) ? tempWidth : containerWidth;

                    filterOptions();
                };

                $scope.onFocus = function(){
                    filterOptions();
                    $scope.optionsVisible = true;
                };

                $scope.removeValue = function(index){
                    $scope.values.splice(index, 1);
                    filterOptions();
                };

                $scope.removeValues = function(){
                    clearValues();
                    filterOptions();
                };

                $scope.setFocus = function(){
                    if (!$scope.optionsVisible)
                        input.focus();
                };
            }
        }

    }]);