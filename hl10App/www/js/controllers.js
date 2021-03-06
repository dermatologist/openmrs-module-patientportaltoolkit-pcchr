angular.module('starter.controllers', ['ngResource', 'starter.services'])

.controller('DashCtrl', function($scope, Post) {
  $scope.posts = Post.query();

  $scope.newPost = function(){
    //alert("saving");
    var input = new Post("");
    input.$save().then(function(res){
      alert(res.message);
    });
  }

})

.controller('ChatsCtrl', function($scope, Chats) {
  // With the new view caching in Ionic, Controllers are only called
  // when they are recreated or on app start, instead of every page change.
  // To listen for when this page is active (for example, to refresh data),
  // listen for the $ionicView.enter event:
  //
  //$scope.$on('$ionicView.enter', function(e) {
  //});

  $scope.chats = Chats.all();
  $scope.remove = function(chat) {
    Chats.remove(chat);
  };
})

.controller('ChatDetailCtrl', function($scope, $stateParams, Chats) {
  $scope.chat = Chats.get($stateParams.chatId);
})

/*
.controller('SignInCtrl', function($scope, $state) {

  $scope.signIn = function(user) {
    console.log('Sign-In', user);
    $state.go('tab.dash');
  };

})
*/
.controller('SignInCtrl',
    ['$scope', '$rootScope', '$state', 'AuthenticationService',
    function ($scope, $rootScope, $state, AuthenticationService) {
        // reset login status
        AuthenticationService.ClearCredentials();

        $scope.signIn = function (user) {
            $scope.dataLoading = true;
            AuthenticationService.SetCredentials(user.username, user.password);
            AuthenticationService.Login(user.username, user.password, function (response) {
                if (response.authenticated) {
                    //AuthenticationService.SetCredentials(user.username, user.password);
                    //location.path('/');
                    $state.go('tab.dash');
                } else {
                    alert("sorry");
                    AuthenticationService.ClearCredentials(user.username, user.password);
                    $scope.error = response.message;
                    $scope.dataLoading = false;
                }
            });
        };
}])


.controller('AccountCtrl', function($scope) {
  $scope.settings = {
    enableFriends: true
  };
});


