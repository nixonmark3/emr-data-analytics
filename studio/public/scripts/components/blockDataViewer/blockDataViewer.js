'use strict';

var blockDataViewerApp = angular.module('blockDataViewerApp', []).directive('blockDataViewer', function () {

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



                /*$scope.pages = [{name: 'Statistics', data: [{"name": "630_MASS_FRAC_C5",
                    "std" : 0.1512856681655764,
                    "mean" : 0.2757579020697675,
                    "min" : 0.09067086000000001,
                    "fifty" : 0.222435572,
                    "count" : 43,
                    "max" : 0.571690008,
                    "twentyFive" : 0.163085218,
                    "seventyFive" : 0.403336692},
                    {"name" : "Pressure",
                        "std" : 3.304426112926163,
                        "mean" : 18.85407301286819,
                        "min" : 7.99486,
                        "fifty" : 19.99857,
                        "count" : 628993,
                        "max" : 27.39091,
                        "twentyFive" : 19.96193,
                        "seventyFive" : 20.00355},
                    {"name" : "PressureA",
                        "std" : 3.304426112926163,
                        "mean" : 18.85407301286819,
                        "min" : 7.99486,
                        "fifty" : 19.99857,
                        "count" : 628993,
                        "max" : 27.39091,
                        "twentyFive" : 19.96193,
                        "seventyFive" : 20.00355},
                    {"name" : "PressureB",
                        "std" : 3.304426112926163,
                        "mean" : 18.85407301286819,
                        "min" : 7.99486,
                        "fifty" : 19.99857,
                        "count" : 628993,
                        "max" : 27.39091,
                        "twentyFive" : 19.96193,
                        "seventyFive" : 20.00355},
                    {"name" : "PressureC",
                        "std" : 3.304426112926163,
                        "mean" : 18.85407301286819,
                        "min" : 7.99486,
                        "fifty" : 19.99857,
                        "count" : 628993,
                        "max" : 27.39091,
                        "twentyFive" : 19.96193,
                        "seventyFive" : 20.00355}] },
                    {name: 'Plot', data: null },
                    {name: 'Results', data: null }];*/

                var init = function(){

                    $scope.getBlockData('Pages',
                        $scope.block.name,
                        function(results){

                            $scope.pages = [];
                            for(var i = 0; i < results.length; i++){
                                $scope.pages.push({name: results[i], data: null});
                            }

                            setPageName();

                            getData();
                        });

                };

                var getData = function(){

                    var page = $scope.pages[$scope.pageIndex];

                    if (page.data == null) {
                        $scope.getBlockData(page.name,
                            $scope.block.name,
                            function (results) {

                                page.data = results;
                            });
                    }

                };

                var setPageName = function(){
                    $scope.pageName = $scope.pages[$scope.pageIndex].name;
                };

                init();
            }
        }
    });

