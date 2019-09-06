layui.extend({
    //你的webuploader.js路径
    webuploader: 'lib/uploader/webuploader'
}).define(['layer','laytpl','table','element','webuploader'],function(exports){
    var $ = layui.$
        ,webUploader= layui.webuploader
        ,element = layui.element
        ,layer=layui.layer
        ,table=layui.table
        ,rowData=[]//保存上传文件属性集合,添加table用
        ,fileSize=10000*1024*1024//默认上传文件大小
        ,fileType
        ,uplaod;
    //加载样式
    layui.link('/assets/lib/uploader/webuploader.css');

    var Class = function (options) {
        var that = this;
        that.options=options;
        that.register();
        that.init();
        that.events();
    };

    Class.prototype.init=function(){
        var that = this,
            options=that.options;
        if(!that.strIsNull(options.size)){
            fileSize=options.size
        }
        if(!that.strIsNull(that.options.fileType)){
            fileType=that.options.fileType;
        }
        layer.open({
            type: 1,
            area: ['80%', '80%'], //宽高
            resize:false,
            content:
            '<script type="text/html" id="button-toolbar"><div class="layui-btn-fluid"><button  id="extend-upload-chooseFile" class="layui-btn layui-btn-sm layui-btn-normal">选择文件</button>'+
            '<button id="extent-button-uploader" lay-event="upload" class="layui-btn layui-btn-sm" >开始上传</button></div></script>'+
            '<table class="layui-table" id="extend-uploader-form" lay-filter="extend-uploader-form">' +
                '  <thead>' +
            '    <tr>' +
            '      <th lay-data="{type:\'numbers\', fixed:\'left\'}"></th>' +
            '      <th lay-data="{field:\'fileName\', minWidth:150}">文件名称</th>' +
            '      <th lay-data="{field:\'fileSize\', minWidth:100}">文件大小</th>' +
            '      <th lay-data="{field:\'validateMd5\', minWidth:80}">文件验证</th>' +
            '      <th lay-data="{field:\'progress\',minWidth: 150,templet:\'#button-form-optProcess\'}">进度</th>' +
            '      <th lay-data="{field:\'oper\', minWidth:100,templet: \'#button-form-uploadTalbe\'}">操作</th>' +
            '    </tr>' +
            '  </thead>'+
            '</table>'+
            '<script type="text/html" id="button-form-uploadTalbe">'+
                '<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="del">删除</a>'+
            '</script>'+
            '<script type="text/html" id="button-form-optProcess">' +
                '<div style="margin-top: 5px;" class="layui-progress layui-progress-big" lay-filter="{{d.fileId}}"  lay-showPercent="true">'+
                  '<div class="layui-progress-bar layui-bg-blue" lay-percent="0%"></div>'+
                '</div>'+
            '</script>'
            ,

            success: function(layero, index){
                table.init('extend-uploader-form',{
                    toolbar:'#button-toolbar',
                    defaultToolbar:false,
                    height: 'full',
                    unresize:true
                });
                uplaod = webUploader.create({
                    // 不压缩image
                    resize: false,
                    // swf文件路径
                    swf:  'src/lib/extend/uploader/Uploader.swf',
                    // 默认文件接收服务端。
                    server: options.url,
                    pick: '#extend-upload-chooseFile',
                    fileSingleSizeLimit:fileSize//单个文件大小
                    //接收文件类型--自行添加options
                    // accept:[{
                    //     title: 'file',
                    //     extensions: fileType,
                    //     mimeTypes: that.buildFileType(fileType)
                    // }]
                });
            }//可以自行添加按钮关闭,关闭请清空rowData
            ,end:function () {
                rowData=[];
                if(options.success){
                    if(typeof options.success==='function') {
                        options.success();
                    }
                }
            }
        });
    };

    Class.prototype.formatFileSize=function(size){
        var fileSize =0;
        if(size/1024/1024>1024){
            var len = size/1024/1024/1024;
            fileSize = len.toFixed(2)+"GB";
        }else if(size/1024>1024){
            var len = size/1024/1024;
            fileSize = len.toFixed(2) +"MB";
        }else{
            var len = size/1024;
            fileSize = len.toFixed(2)+"KB";
        }
        return fileSize;
    };

    Class.prototype.buildFileType=function (type) {
        var ts = type.split(',');
        var ty='';

        for(var i=0;i<ts.length;i++){
            ty=ty+ "."+ts[i]+",";
        }
        return  ty.substring(0, ty.length - 1)
    };

    Class.prototype.strIsNull=function (str) {
        return typeof str === "undefined" || str == null || str === "";
    };

    Class.prototype.events=function () {
        var that = this;
        //当文件添加进去
        uplaod.on('fileQueued', function( file ){
            var fileSize = that.formatFileSize(file.size);
            var row={fileId:file.id,fileName:file.name,fileSize:fileSize,validateMd5:'0%',progress:file.id,state:'就绪'};
            rowData.push(row);
            that.reloadData(rowData);
            element.render('progress');
        });

        //监听进度条,更新进度条信息
        uplaod.on( 'uploadProgress', function( file, percentage ) {
            element.progress(file.id, (percentage * 100).toFixed(0)+'%');
        });


        //错误信息监听
        uplaod.on('error', function(handler){
            if(handler==='F_EXCEED_SIZE'){
                layer.msg('上传的单个太大!。<br>操作无法进行,如有需求请联系管理员', {icon: 5});
            }else if(handler==='Q_TYPE_DENIED'){
                layer.msg('不允许上传此类文件!。<br>操作无法进行,如有需求请联系管理员', {icon: 5});
            }
        });


        //移除上传的文件
        table.on('tool(extend-uploader-form)', function(obj){
            var data = obj.data;
            if(obj.event === 'del'){
                that.removeArray(rowData,data.fileId);
                uplaod.removeFile(data.fileId,true);
                obj.del();
            }
        });

        table.on('toolbar(extend-uploader-form)',function (obj) {
            if (obj.event==='upload'){
                that.uploadToServer();
            }
        });

        // //开始上传
        // $("#extent-button-uploader").click(function () {
        //     that.uploadToServer();
        //
        // });

        //单个文件上传成功
        uplaod.on( 'uploadSuccess', function( file ) {
            that.setTableBtn(file.id,'完成');
        });

        //所有文件上传成功后
        uplaod.on('uploadFinished',function(){//成功后
            layer.alert('上传成功',{
                icon:1,
                time:2000
            });
            setTimeout(function () {
                layer.closeAll('page');
            },2000);
            // var btn=$("#extent-button-uploader");
            // $(btn).text("开始上传");
            // $(btn).removeClass('layui-btn-disabled');
        });

    };

    Class.prototype.reloadData=function(data){
        layui.table.reload('extend-uploader-form',{
            data : data
        });
    };

    Class.prototype.register=function () {
        var that = this,
            options = that.options;

        if(that.strIsNull(options.md5)) {
            return;
        }
        // 在文件开始发送前做些异步操作。做md5验证
        // WebUploader会等待此异步操作完成后，开始发送文件。
        webUploader.Uploader.register({
            "before-send-file":"beforeSendFile"
        },{
            beforeSendFile: function(file){
                var task = new $.Deferred();
                (new webUploader.Uploader()).md5File(file, 0, 10*1024*1024).progress(function(percentage){
                    var v = that.getTableHead('validateMd5');
                    var table = $("#extend-uploader-form").next().find('div[class="layui-table-body layui-table-main"]').find('table');
                    var pro = table.find('td[data-field="progress"]');
                    for(var i=0;i<pro.length;i++){
                        var d = $(pro[i]).attr('data-content');
                        if(d===file.id ){
                            var t = $(pro[i]).prev();
                            t.empty();
                            t.append('<div class="'+v+'">'+(percentage * 100).toFixed(0)+'%</div>');
                        }
                    }
                }).then(function(val){
                    $.ajax({
                        type: "POST"
                        , url: options.md5
                        , data: {
                            id:file.id,fileName:file.name,fileSize:file.size,md5: val //后台接收 String md5
                        }
                        , cache: false
                        , timeout: 3000
                        , dataType: "json"
                    }).then(function(data, textStatus, jqXHR){
                        if(data.code===200){   //若存在，这返回失败给WebUploader，表明该文件不需要上传
                            task.reject(); //
                            uplaod.skipFile(file);
                            that.setTableBtn(file.id,'秒传');
                            element.progress(file.id,'100%');
                        }else{
                            task.resolve();
                        }
                    }, function(jqXHR, textStatus, errorThrown){    //任何形式的验证失败，都触发重新上传
                        task.reject();
                        uplaod.skipFile(file);
                        that.setTableBtn(file.id,'md5校验失败');
                    });
                });
                return $.when(task);
            }
        });
    };


    /***
     * 注意更改了table列的位置,或自行新增了表格,请自行在这修改
     */
    Class.prototype.getTableHead=function (field) {
        //获取table头的单元格class,保证动态设置table内容后单元格不变形
        var div = $("#extend-uploader-form").next().find('div[class="layui-table-header"]');
        var div2 = div[0];
        var table = $(div2).find('table');
        var td = table.find('th[data-field="'+field+'"]').find('div').attr('class');
        return td;
    };

    Class.prototype.setTableBtn=function (fileId,val) {
        var td = this.getTableHead('oper');
        //获取操作栏,修改其状态
        var table = $("#extend-uploader-form").next().find('div[class="layui-table-body layui-table-main"]').find('table');
        var pro = table.find('td[data-field="progress"]');
        for(var i=0;i<pro.length;i++){
            var d = $(pro[i]).attr('data-content');
            if(d===fileId ){
                var t = $(pro[i]).next();
                t.empty();
                t.append('<div class="'+td+'"><a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="ok">'+val+'</a></div>')
            }
        }
    };


    Class.prototype.uploadToServer=function () {
        if(rowData.length<=0){
            layer.msg('没有上传的文件', {icon: 5});
            return;
        }
        $("#extent-button-uploader").text("正在上传");
        $("#extent-button-uploader").addClass('layui-btn-disabled');
        uplaod.upload();
    };

    Class.prototype.removeArray=function (array,fileId) {
        for(var i=0;i<array.length;i++){
            if(array[i].fileId===fileId){
                array.splice(i,1);
            }
        }
        return array;
    };

    var layUploader = {
        render:function (options) {
            var inst = new Class(options);
            return inst;
        }

    };

    exports('layUploader', layUploader);
});