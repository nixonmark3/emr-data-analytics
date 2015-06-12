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

                    for (var column = 1; column < $scope.chartData.length; column++) {

                        var columnData = $scope.chartData[column].slice($scope.currentIndex, ($scope.currentIndex + windowSize));

                        $scope.showing[column-1] = $scope.showing[column-1].concat(formatData(column, columnData));
                    }

                    $scope.currentIndex = $scope.currentIndex + windowSize;
                };

                var prepareData = function() {

                    if (!$scope.chartData) return;

                    $scope.showing = [];
                    $scope.columnNames = ['', 'Timestamp'];
                    $scope.currentIndex = 0;

                    $scope.columnNames = $scope.columnNames.concat($scope.chartData[0]);

                    for (var column = 1; column < $scope.chartData.length; column++) {

                        var columnData = $scope.chartData[column].slice(0, windowSize);

                        columnData = formatData(column, columnData);

                        columnData.unshift($scope.columnNames[column]);

                        $scope.showing.push(columnData);
                    }

                    $scope.currentIndex = windowSize;
                };

                var formatData = function(index, data) {

                    if (index == 1) {

                        data = data.map(formatUnixTime);
                    }
                    else {

                        data = data.map(formatNumber);
                    }

                    return data;
                };

                var formatUnixTime = function(item) {

                    var date = new Date(item * 1000);
                    return date.toISOString().replace('T', ' ').replace('Z', '');
                }

                var formatNumber = function(item) {

                    return item.toFixed(5);
                };

                prepareData();
            }
        }
    }]);