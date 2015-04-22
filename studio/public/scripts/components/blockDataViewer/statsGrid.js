'use strict';

blockDataViewer.directive('statsGrid', [function () {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/blockDataViewer/statsGrid.html',
            scope: {
                block: '=',
                getData: '='
            },
            link: function ($scope) {

                $scope.data = [{"name": "630_MASS_FRAC_C5",
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
                        "seventyFive" : 20.00355}];

                $scope.gridWidth = ($scope.data.length + 1) * 150;

            }
        }
    }]);

