'use strict';

var blockDataViewerApp = angular.module('blockDataViewerApp', []).directive('blockDataViewer', function () {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/blockDataViewer/blockDataViewer.html',
            scope: {

            },
            link: function ($scope, element, attrs) {

                $scope.pageIndex = 0;

                $scope.isCurrentPage = function(index){
                    return $scope.pageIndex === index;
                };

                $scope.block = {name: 'Columns1'};

                $scope.pageLeft = function() {
                    if ($scope.pageIndex > 0) {
                        $scope.pageIndex = $scope.pageIndex - 1;
                    }
                }

                $scope.pageRight = function() {
                    if ($scope.pageIndex < ($scope.pages.length - 1)) {
                        $scope.pageIndex = $scope.pageIndex + 1;
                    }
                }

                $scope.pages = ['statistics', 'plot', 'results'];

            }
        }
    });

