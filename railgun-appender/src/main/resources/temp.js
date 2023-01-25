(function(off){
  if(!off) return;
  layui.disuse('table').extend({
    table: '{/}//ww:5018/layui/2.6.13/src/modules/table'
  });
})();
urlroot = "http://localhost:8080"
urlConfig = {
    #if($config.hasEdit)
    updateUrl : urlroot + "/$po.getSimpleName()/update"
    #end

}
layui.use(['table', 'dropdown'], function(){
  var table = layui.table;
  var laytpl = layui.laytpl;
  var dropdown = layui.dropdown;
  var form = layui.form;


  // 仅用于各类测试的表头
  var test_cols = [[
    {type: 'checkbox', fixed: 'left'}
    ,{field:'id', title:'ID', width:80, fixed: 'left', unresize: true, sort: true, totalRowText: '合计：'}
    ,{field:'username', title:'用户名', width:120, edit: 'text'}
    ,{field:'email', title:'邮箱 <i class="layui-icon layui-icon-email"></i>', hide: 0, width:150, edit: 'text'}
    ,{field:'sex', title:'性别', width:80, edit: 'text', sort: true}
    ,{field:'city', title:'城市', width: 120}
    ,{field:'sign', title:'签名'}
    ,{field: 'experience', title: '积分', width:80, sort: true, align:'center', totalRow: '{{ d.TOTAL_NUMS }} 😊'}
    ,{fixed: 'right', title:'操作', toolbar: '#barDemo', width:150}
  ]];

  // 全局设定某参数
  table.set({
    where: {
      token: '默认 token 参数'
    }
    //,defaultToolbar: ['filter']
    ,limit: 30
    //,url: 'list'
    //,height: 300
  });

  //渲染
  window.ins1 = table.render({
    elem: '#test'
    ,height: 520
    //,width: 600
    ,title: '用户数据表'
    ,url: '../examples/json/table/demo1.json'
    //,method: 'post'


    //,lineStyle: 'height: 95px;' // 行样式
    ,css: [ // 自定义样式
      '.layui-table-page{text-align: right;}'
      ,'.layui-table-pagebar{float: left;}'
    ].join('')
    //,className: '.demo-table-view'

    //,size: 'sm'
    //,skin: 'line'
    //,loading: false

    ,pagebar: '#pagebarDemo' // 分页栏模板
    ,page: !1 ? false : {
      //curr: layui.data('tableCache').curr || 1 // 读取记录中的页码，赋值给起始页
    }
    ,limit: 30
    ,toolbar: '#toolbarDemo'
    ,defaultToolbar: ['filter', 'exports', 'print', {
      title: '帮助'
      ,layEvent: 'LAYTABLE_TIPS'
      ,icon: 'layui-icon-tips'
    }]
    //,escape: false
    ,editTrigger: 'dblclick'
    //,cellMaxWidth: 320
    ,cols: !1 ? test_cols : [[
      {type: 'checkbox', fixed: 'left'}
    #foreach($element in $dto.fields)
      ,{field:'$element.name', title:'$element.comment', sort: true}
    #end

      ,{fixed: 'right', title:'操作', width: 180, templet: function(d) {
        var btnsStr = "";
        #if($!config.hasEdit)
            btnsStr += "<a class="layui-btn layui-btn-xs" lay-event="edit">编辑</a>";
        #end
        #if($!config.hasDel)
            btnsStr += "<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">编辑</a>";
        #end
        return btnsStr;
      }}
    ]]

    //,autoSort: false // 禁用前端自动排序，由服务的完成排序
    ,initSort111: { // 初始排序状态
      field: 'experience' //排序字段，对应 cols 设定的各字段名
      ,type: 'desc' //排序方式  asc: 升序、desc: 降序、null: 默认排序
    }

    ,headers: {headers_token: 'sasasas'}
    ,where: {
      test: '初始 test 参数'
      ,token: '初始 token'
      ,key: 'experience'
      ,order: 'asc'
    }
    ,before: function(options){
      options.where.AAAAA = 123;
      //console.log(options)
    }
    ,done: function(res, curr, count){
      var id = this.id;

      // 记录当前页码
      /*
      layui.data('tableCache', {
        key: 'curr',
        value: curr
      });
      */

      // 重载测试
      dropdown.render({
        elem: '#reloadTest' //可绑定在任意元素中，此处以上述按钮为例
        ,data: [{
          id: 'reload',
          title: '重载'
        },{
          id: 'reload-deep',
          title: '重载 - 参数叠加'
        },{
          id: 'reloadData',
          title: '仅重载数据'
        },{
          id: 'reloadData-deep',
          title: '仅重载数据 - 参数叠加'
        }]
        // 菜单被点击的事件
        ,click: function(obj){
          switch(obj.id){
            case 'reload':
              // 重载 - 默认（参数重置）
              table.reload('test', {
                where: {
                  abc: '123456'
                  //,test: '新的 test2'
                  //,token: '新的 token2'
                }
                ,height: 'full-130' // 重载高度
                /*
                ,cols: [[ // 重置表头
                  {type: 'checkbox', fixed: 'left'}
                  ,{field:'id', title:'ID', width:80, fixed: 'left', unresize: true, sort: true, totalRowText: '合计：'}
                  ,{field:'sex', title:'性别', width:80, edit: 'text', sort: true}
                  ,{field:'experience', title:'积分', width:80, sort: true, totalRow: true, templet: '<div>{{ d.experience }} 分</div>'}
                  ,{field:'logins', title:'登入次数', width:100, sort: true, totalRow: true}
                  ,{field:'joinTime', title:'加入时间', width:120}
                ]]
                */

              });
            break;
            case 'reload-deep':
              // 重载 - 深度（参数叠加）
              table.reload('test', {
                where: {
                  abc: 123
                  ,test: '新的 test1'
                }
                //,defaultToolbar: ['print'] // 重载头部工具栏右侧图标
                ,page: {curr: 5, limit: 20}
                //,cols: ins1.config.cols
              }, true);
            break;
            case 'reloadData':
              // 数据重载 - 参数重置
              table.reloadData('test', {
                where: {
                  abc: '123456'
                  //,test: '新的 test2'
                  //,token: '新的 token2'
                }
                ,height: 2000 // 测试无效参数
                //,url: '404'
                //,elem: null
                //,page: {curr: 5, limit: 20}
                ,scrollPos: 'fixed' // 保持滚动条位置不变
              });
            break;
            case 'reloadData-deep':
              // 数据重载 - 参数叠加
              table.reloadData('test', {
                where: {
                  abc: 123
                  ,test: '新的 test1'
                }
              }, true);
            break;
          }
          layer.msg('可观察 Network 请求参数的变化');

        }
      });


      // 更多测试
      dropdown.render({
        elem: '#moreTest' //可绑定在任意元素中，此处以上述按钮为例
        ,data: [{
          id: 'add',
          title: '添加'
        },{
          id: 'update',
          title: '编辑'
        },{
          id: 'delete',
          title: '删除'
        }]
        //菜单被点击的事件
        ,click: function(obj){
          var checkStatus = table.checkStatus(id)
          var data = checkStatus.data; // 获取选中的数据

          switch(obj.id){
            case 'add':
              layer.open({
                title: '添加',
                type: 1,
                area: ['80%','80%'],
                content: '<div style="padding: 16px;">自定义表单元素</div>'
              });
            break;
            case 'update':
              layer.open({
                title: '编辑',
                type: 1,
                area: ['80%','80%'],
                content: '<div style="padding: 16px;">自定义表单元素</div>'
              });
            break;
            case 'delete':
              if(data.length === 0){
                return layer.msg('请选择一行');
              }
              layer.msg('delete event');
            break;
          }
        }
      });
    }

    ,error: function(res, msg){
      console.log(res, msg)
    }

    /*
    ,request: { // 自定义请求参数名称
      pageName: 'curr' //页码的参数名称，默认：page
      ,limitName: 'nums' //每页数据量的参数名，默认：limit
    }
    ,parseData: function(res){ // 任意数据格式的解析
      return {
        "status": res.status
        ,"msg": res.message
        ,"count": res.total
        ,"data": res.data.list
      };
    }
    */
  });

  //排序事件
  table.on('sort(test)', function(obj){
    //console.log(obj);

    //return;
    layer.msg('服务端排序。order by '+ obj.field + ' ' + obj.type);

    //服务端排序
    table.reloadData('test', {
      //initSort: obj,
      //page: {curr: 1}, //重新从第一页开始
      where: { // 向服务端传入排序参数
        key: obj.field, //排序字段
        order: obj.type //排序方式
      }
    });
  });

  // 工具栏事件
  table.on('toolbar(test)', function(obj){
    var id = obj.config.id;
    var checkStatus = table.checkStatus(id);

    switch(obj.event){
      case 'getCheckData':
        var data = checkStatus.data;
        layer.alert(layui.util.escape(JSON.stringify(data)));
      break;
      case 'getData':
        var getData = table.getData(id);
        console.log(getData);
        layer.alert(layui.util.escape(JSON.stringify(getData)));
      break;
      case 'isAll':
        layer.msg(checkStatus.isAll ? '全选': '未全选')
      break;
      case 'LAYTABLE_TIPS':
        layer.alert('Table for layui-v'+ layui.v);
      break;
    };
  });
  #if($!config.hasEdit)
  renderObject2Form = function (obj) {
    var input = null;
    #foreach($element in $dto.fields)
      input = $('#updateForm input[name="$element.name"');
      input.value(obj.$element.name);
    #end
  }
  #end

  //触发单元格工具事件
  table.on('tool(test)', function(obj){ // 双击 toolDouble
    var data = obj.data;
    //console.log(obj)
    #if($!config.hasDel)
    if(obj.event === 'del'){
      layer.confirm('真的删除行么', function(index){
        obj.del();
        $.ajax({
            url:
        })
        layer.close(index);
      });
    }#end #if($!config.hasEdit) else if(obj.event === 'edit'){

        $('#updateForm').show();
     #end
    }
  });



  //触发表格复选框选择
  table.on('checkbox(test)', function(obj){
    console.log(obj)
  });

  //触发表格单选框选择
  table.on('radio(test)', function(obj){
    console.log(obj)
  });

  // 行单击事件
  table.on('row(test)', function(obj){
    //console.log(obj);
    //layer.closeAll('tips');
  });
  // 行双击事件
  table.on('rowDouble(test)', function(obj){
    console.log(obj);
  });

  // 单元格编辑事件
  table.on('edit(test)', function(obj){
    var field = obj.field // 得到字段
    var value = obj.value // 得到修改后的值
    var oldValue = obj.oldValue // 得到修改前的值 -- v2.8.0 新增
    var data = obj.data; // 得到当前编辑所在行的数据

    // 值的校验
    if(field === 'email'){
      if(!/^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/.test(obj.value)){
        layer.tips('输入的邮箱格式不正确，请重新编辑', this, {tips: 1});
        return obj.reedit(); // 重新编辑 -- v2.8.0 新增
      }
    }

    // 编辑后续操作，如提交更新请求，以完成真实的数据更新
    // …
    layer.msg('编辑成功', {icon: 1});

    // 其他更新操作
    var update = {};
    update[field] = value;
    obj.update(update, true); // 参数 true 为 v2.7.4 新增功能，即同步更新其他包含自定义模板并可能存在关联的列视图
  });


  // 列拖拽宽度后的事件 -- v2.8.0 新增
  table.on('colResized(test)', function(obj){
    console.log(obj);
  });

  // 列拖拽宽度后的事件 -- v2.8.0 新增
  table.on('colToggled(test)', function(obj){
    console.log(obj);
  });
});