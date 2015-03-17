app.controller('MainCtrl', function($scope, $http) {
    $scope.alerts = [];

    $scope.addAlert = function(t, m) {
        $scope.alerts.push({type: t, msg: m});
    };

    $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
    };

    $scope.createDefinitions = function() {
        console.log("create definitions");
        $http.post('/definitions').
            success(function() {
                $scope.addAlert('success', 'Definitions created successfully!');
            }).
            error(function() {
                $scope.addAlert('danger', 'Error creating definitions!');
            });
    };

    $scope.createTestDiagram = function() {
        console.log("create test diagram");
        $http.post('/diagram').
            success(function() {
                $scope.addAlert('success', 'Test diagram created successfully!');
            }).
            error(function() {
                $scope.addAlert('danger', 'Error creating test diagram!');
            });
    };
});