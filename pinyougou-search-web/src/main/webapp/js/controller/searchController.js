app.controller('searchController',function($scope,searchService){
    //绑定了一个变量
    $scope.searchMap={'keywords':'','category':'','brand':'',spec:{}};
    //搜索
    $scope.search=function(){
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
            }
        );
    }

    /**
     * 在点击分类 或者品牌 或者规格的时候被调用
     * key：category
     * value:手机
     */
    $scope.addSearchItem=function (key,value) {

        //添加普通的搜索项
        if(key=='category'|| key=='brand'){
            $scope.searchMap[key]=value;
        }else{
            //添加的是规格的搜索项  spec:{"网络":"移动3G"}
            $scope.searchMap.spec[key]=value;
        }

        $scope.search();



    }
    //移除搜索项
    $scope.removeSearchItem=function (key) {

        //添加普通的搜索项
        if(key=='category'|| key=='brand'){
            $scope.searchMap[key]='';
        }else{
            //添加的是规格的搜索项  spec:{"网络":"移动3G"}
            delete $scope.searchMap.spec[key];//删除对象中的属性
        }
        $scope.search();
    }
});
