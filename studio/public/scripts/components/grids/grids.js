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

                var windowSize = 50;
                var raw = element[0];

                $scope.showing = [];
                $scope.columnNames = [];
                $scope.currentIndex = 0;
                $scope.gridWidth = 0;

                element.bind('scroll', function() {

                    if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight - 1) {

                        $scope.$apply(loadMoreData());
                    }
                });

                $scope.$watch("chartData", function () {

                    prepareData();
                });

                var loadMoreData = function() {

                    loadDataByRows();

                    $scope.currentIndex = $scope.currentIndex + windowSize;
                };

                var prepareData = function() {

                    if (!$scope.chartData) return;

                    $scope.showing = [];
                    $scope.currentIndex = 0;

                    var columnNames = ['Index'];
                    columnNames = columnNames.concat($scope.chartData[0]);
                    $scope.showing.push(columnNames);

                    loadDataByRows();

                    $scope.currentIndex = windowSize;

                    $scope.gridWidth = calculateGridWidth(columnNames.length);
                };

                var calculateGridWidth = function(numberOfColumns) {

                    var width = 0;

                    if (numberOfColumns < 5) {

                        width = 207 * numberOfColumns;
                    }
                    else if (numberOfColumns < 10) {

                        width = 190 * numberOfColumns;
                    }
                    else {

                        width = 185 * numberOfColumns;
                    }

                    return width;
                };


                var formatUnixTime = function(item) {

                    var date = new Date(item * 1000);

                    var validDate = !isNaN(date.valueOf());

                    if (!validDate) {

                        return item;
                    }

                    return date.toISOString().replace('T', ' ').replace('Z', '');
                };

                var formatNumber = function(item) {

                    if (isNaN(item)) {

                        return 'NaN';
                    }

                    return item.toFixed(5);
                };

                var loadDataByRows = function() {

                    var stoppingPoint = $scope.currentIndex + windowSize;

                    var numberOfObservations = $scope.chartData[1].length;

                    if (stoppingPoint >= numberOfObservations) {

                        stoppingPoint = numberOfObservations - 1;
                    }

                    for (var rowIndex = $scope.currentIndex; rowIndex <= stoppingPoint; rowIndex++) {

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
    }])

    .directive('featuresGrid', ['$window', function($window){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/grids/featuresGrid.html',
            scope: {
                features: "=",
                columnWidth: "=",
                rowHeaderWidth: "=",
                onPage: "="
            },
            link: function ($scope, element, attrs) {

                var recordCount = 0;
                var featureOffset = 2;
                var page = 0;
                var pageSize = 100;
                var indexType = $scope.features[0].dtype;

                // initialize position variables
                $scope.padding = 10;
                $scope.rowHeight = 36;
                $scope.columnHeaderPosition = 0;
                $scope.rowHeaderPosition = 0;
                $scope.originX = $scope.rowHeaderWidth + 2 * $scope.padding;
                $scope.originY = $scope.rowHeight + 2 * $scope.padding;
                $scope.gridFeatures = determineFeatures();

                function determineFeatures() {

                    var featureList = [];

                    for(var feature in $scope.features) {

                        if (feature > 0) {
                            featureList.push($scope.features[feature]);
                        }
                    }

                    return featureList;
                }

                var featureCount = $scope.gridFeatures.length;
                $scope.gridWidth = (featureCount * ($scope.columnWidth + $scope.padding) + $scope.originX);
                setGridHeight();

                $scope.indexes = [];
                $scope.data = [];

                angular.element('#grid-content-container').bind('scroll', function(event) {

                    $scope.$apply(function() {

                        $scope.columnHeaderPosition = -1 * event.target.scrollLeft;
                        $scope.rowHeaderPosition = -1 * event.target.scrollTop;
                    });

                });

                function init(){

                    $scope.gridFeatures.forEach(function(feature) {

                        for (var i in feature.statistics) {

                            if (['count', 'missing', 'dtype'].indexOf(i) === -1) {

                                feature.statistics[i] = formatNumber(feature.statistics[i]);
                            }
                        }
                    });

                    $scope.onPage().then(function(data) {

                        $scope.indexes = data[1].slice((page * pageSize), ((page + 1) * pageSize));

                        if (indexType == 'datetime64[ns]' || indexType == 'float64') {

                            for (var item in $scope.indexes) {

                                $scope.indexes[item] = formatUnixTime($scope.indexes[item]);
                            }
                        }
                        else {

                            for (var item in $scope.indexes) {

                                $scope.indexes[item] = parseInt(item) + 1;
                            }
                        }

                        recordCount = $scope.indexes.length;
                        setGridHeight();

                        for(var feature in $scope.gridFeatures) {

                            var featureIndex = data[0].indexOf($scope.gridFeatures[feature].column) + featureOffset;
                            $scope.data.push(data[featureIndex].slice((page * pageSize), ((page + 1) * pageSize)).map(formatNumber));
                        }

                        page++;
                    });
                }

                function setGridHeight(){

                    $scope.gridHeight = ((recordCount + 10) * $scope.rowHeight + 3 * $scope.padding);
                }

                var formatUnixTime = function(item) {

                    var date = new Date(item * 1000);

                    var validDate = !isNaN(date.valueOf());

                    if (!validDate) {

                        return item;
                    }

                    return date.toISOString().replace('T', ' ').replace('Z', '');
                };

                var formatNumber = function(item) {

                    if (isNaN(item)) {

                        return 'NaN';
                    }

                    var n = 0.01;

                    if (item != 0) {

                        if (Math.abs(item) < n) {

                            return item.toExponential(5);
                        }
                    }

                    return item.toFixed(5);
                };

                init();
            }
        }
    }]);