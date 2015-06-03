'use strict';

var blockDataViewerApp = angular.module('blockDataViewerApp', [])
    .directive('blockDataViewer', function ($timeout) {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/blockDataViewer/blockDataViewer.html',
            scope: {
                block: "=",
                getBlockData: "="
            },
            link: function ($scope, element, attrs) {

                $scope.pageIndex = 0;
                $scope.pageName = "";
                $scope.pagingForward = true;

                $scope.isCurrentPage = function(index){
                    return $scope.pageIndex === index;
                };

                $scope.pageLeft = function() {
                    if ($scope.pageIndex > 0) {
                        $scope.pagingForward = false;
                        $scope.pageIndex = $scope.pageIndex - 1;
                        setPageName();
                        getData();
                    }
                };

                $scope.pageRight = function() {
                    if ($scope.pageIndex < ($scope.pages.length - 1)) {
                        $scope.pagingForward = true;
                        $scope.pageIndex = $scope.pageIndex + 1;
                        setPageName();
                        getData();
                    }
                };

                //
                // Set the width of the Grid based on the number of Features
                //
                $scope.setGridWidth = function(columns) {

                    if (columns != null) {

                        var additionalLength = 0;

                        if (columns.length < 3) {
                            additionalLength++;
                        }

                        return (columns.length + additionalLength) * 220;
                    }

                    return 1000;
                };

                var init = function(){

                    $timeout(function(){

                        $scope.getBlockData('Pages',
                            $scope.block.uniqueName,
                            function(results){

                                $scope.pages = [];
                                for(var i = 0; i < results.length; i++){
                                    $scope.pages.push({name: results[i], data: null});
                                }

                                setPageName();

                                getData();
                            });
                    }, 500);

                };

                var getData = function() {

                    if ($scope.pages.length != 0) {

                        var page = $scope.pages[$scope.pageIndex];

                        if (page.data == null) {
                            $scope.getBlockData(page.name,
                                $scope.block.uniqueName,
                                function (results) {

                                    page.data = results;
                                });
                        }
                    }
                };

                var setPageName = function(){

                    if ($scope.pages.length == 0) {
                        $scope.pageName = "";
                    }
                    else {
                        $scope.pageName = $scope.pages[$scope.pageIndex].name;
                    }
                };

                init();
            }
        }
    });

