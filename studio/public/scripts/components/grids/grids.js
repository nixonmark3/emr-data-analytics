'use strict';

angular.module('emr.ui.grids', [])
    .directive('timeSeriesGrid', ['$window', function($window) {

        return {

            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/grids/timeSeriesGrid.html',
            scope: {

                chartData: "="
            },
            link: function ($scope, element, attrs) {

                var windowSize = 100;
                var raw = element[0];

                $scope.showing = [];
                $scope.columnNames = [];
                $scope.currentIndex = 0;

                element.bind('scroll', function() {

                    if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight - 1) {

                        $scope.$apply(loadMoreData());
                    }
                });

                $scope.$watch("chartData", function () {

                    prepareData();
                });

                var loadMoreData = function() {

                    formatData();

                    $scope.currentIndex = $scope.currentIndex + windowSize;
                };

                var prepareData = function() {

                    if (!$scope.chartData) return;

                    $scope.showing = [];
                    $scope.currentIndex = 0;

                    var columnNames = ['Timestamp'];
                    $scope.showing.push(columnNames.concat($scope.chartData[0]));

                    formatData();

                    $scope.currentIndex = windowSize;
                };

                var formatUnixTime = function(item) {

                    var date = new Date(item * 1000);
                    return date.toISOString().replace('T', ' ').replace('Z', '');
                };

                var formatNumber = function(item) {

                    return item.toFixed(5);
                };

                var formatData = function() {

                    for (var rowIndex = $scope.currentIndex; rowIndex <= ($scope.currentIndex + windowSize); rowIndex++) {

                        var row = [];

                        for (var column = 1; column < $scope.chartData.length; column++) {

                            if (column == 1) {

                                row.push(formatUnixTime($scope.chartData[column][rowIndex]));
                            }
                            else {

                                row.push(formatNumber($scope.chartData[column][rowIndex]));
                            }
                        }

                        $scope.showing.push(row);
                    }

                };

                prepareData();
            }
        }
    }]);