'use strict';

angular.module('emr.ui.dashboard', ['gridster'])

    .directive('dashboardPages', ['$window', function($window) {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/dashboard/dashboardPages.html',
            scope: {

            },
            link: function ($scope, element, attrs) {

                $scope.padding = 40;
                $scope.overlap = 15;

                $scope.pages = [
                    {

                    },
                    {

                    },
                    {

                    }
                ];

                $scope.cards = [
                    { sizeX: 2, sizeY: 1, row: 0, col: 0 },
                    { sizeX: 2, sizeY: 2, row: 0, col: 2 },
                    { sizeX: 1, sizeY: 1, row: 0, col: 4 },
                    { sizeX: 1, sizeY: 1, row: 0, col: 5 },
                    { sizeX: 2, sizeY: 1, row: 1, col: 0 },
                    { sizeX: 1, sizeY: 1, row: 1, col: 4 },
                    { sizeX: 1, sizeY: 2, row: 1, col: 5 },
                    { sizeX: 1, sizeY: 1, row: 2, col: 0 },
                    { sizeX: 2, sizeY: 1, row: 2, col: 1 },
                    { sizeX: 1, sizeY: 1, row: 2, col: 3 },
                    { sizeX: 1, sizeY: 1, row: 2, col: 4 }
                ];

                $scope.gridsterOpts = {
                    outerMargin: false
                };

                function init(){
                    setPageWidth();
                }

                function setPageWidth(){
                    $scope.pageWidth = element.parent().width() - (2 * $scope.padding + 2 * $scope.overlap);
                    $scope.positions = [($scope.padding + $scope.overlap - $scope.pageWidth),
                        ($scope.padding + $scope.overlap),
                        ($scope.padding + $scope.overlap + $scope.pageWidth)];
                }

                angular.element($window).bind("resize", function () {

                    $scope.$apply(function () { setPageWidth(); });
                });

                init();
            }
        }
    }]);