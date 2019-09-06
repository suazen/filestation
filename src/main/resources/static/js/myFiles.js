var tableIns;

layui.config({
    base:'/assets/'
}).extend({
    layUploader:'lib/layUploader'
}).use(['jquery', 'table','element','layer','layUploader'], function () {
    var $ = layui.$;
    var table = layui.table;
    var layer=layui.layer;
    var element=layui.element;
    var layUploader=layui.layUploader;
    var folderId=GetQueryString('folderId')?window.location.search.split('=')[1]:null;

    $(document).ready(function() {
        $(document).bind("contextmenu", function (e) {
            return false;
        });
    });

    tableIns = table.render(
        {
            elem: '#table',
            height: 'full',
            toolbar:'#toolbar',
            defaultToolbar:'false',
            skin: 'line',
            size: 'lg',
            loading:true,
            cols: [
                [
                    {field: 'id', hide: true},
                    {field: 'name', title:os.isPc?"名称":"文件列表", sort: true,templet:function (d) {
                            var iconName='folder.svg';
                            if (d.type===1){
                                switch (getFileType(d.name)) {
                                    case 'DOC':
                                    case 'DOCX':
                                        iconName= 'word.svg';
                                        break;
                                    case 'ZIP':
                                    case 'TAR':
                                    case 'RAR':
                                        iconName='zip.svg';
                                        break;
                                    case 'EXE':
                                        iconName='exe.svg';
                                        break;
                                    case 'XLSX':
                                    case 'XLS':
                                        iconName='excel.svg';
                                        break;
                                    case 'PPTX':
                                    case 'PPT':
                                        iconName='ppt.svg';
                                        break;
                                    case 'PDF':
                                        iconName='pdf.svg';
                                        break;
                                    case 'JPG':
                                    case 'PNG':
                                    case 'GIF':
                                        iconName='image.svg';
                                        break;
                                    case 'MP4':
                                    case 'AVI':
                                    case 'RMVB':
                                    case 'MOV':
                                        iconName='video.svg';
                                        break;
                                    case 'MP3':
                                    case 'OGG':
                                    case 'WAV':
                                    case 'AAC':
                                    case 'FLAC':
                                        iconName='music.svg';
                                        break;
                                    case 'TXT':
                                        iconName='text.svg';
                                        break;
                                    case 'APK':
                                        iconName='apk.svg';
                                        break;
                                    default:
                                        return '<a><i class="layui-icon layui-icon-file-b"></i> '+d.name+'</a>';
                                }
                            }
                            return '<a><img class="layui-icon" src="/assets/layui/images/'+iconName+'"> '+d.name+'</a>';
                        }},
                    {field: 'size', title: "大小",width:'15%', sort: true,templet:function (d) {
                            if (d.size===null)
                                return '';
                            return formatSize(d.size);
                        },hide:!os.isPc},
                    {field: 'type', title: "类型",width:'15%', sort: true,templet:function (d) {
                            if (d.type===0)
                                return '文件夹';
                            else {
                                d.type=getFileType(d.name);
                            }
                            return d.type;
                        },hide:!os.isPc},
                    {field: 'date', title: "创建日期",width:'20%',sort: true,hide:!os.isPc},
                    {field:'shared',hide:true}
                ]
            ],
            url: "/file/list",
            where:{
                folderId:folderId
            },
            response: {
                statusCode: 200
            },
            parseData: function (res) {
                return {
                    "code":res.code,
                    "data": res.data.data,
                    "count": res.data.count
                };
            },
            done:function () {
                $('#upload').on('click',function () {
                    layUploader.render({
                        md5:'/file/md5/'+folderId,
                        url: '/file/upload/'+folderId,
                        success:function () {
                            window.location.reload();
                            // tableIns.reload({
                            //     where:{
                            //         folderId:folderId
                            //     }
                            // })
                        }
                    })
                });

                var ele=$('.layui-table-body');
                if (os.isPc){
                    ele.mousedown(function (e) {
                        if (3 === e.which){
                            var point=[(e.clientY + 158) > $(window).height() ? (e.clientY - 158) : e.clientY,
                                (e.clientX+77)>$(window).width()?(e.clientX-77):e.clientX];
                            menuPop(e,point);
                        }
                    });
                }else {
                    ele.on({
                        touchstart: function(e) {
                            // 长按事件触发
                            timeOutEvent = setTimeout(function() {
                                timeOutEvent = 0;
                                var point=[(e.originalEvent.targetTouches[0].clientY + 158) > $(window).height() ? (e.originalEvent.targetTouches[0].clientY - 158) : e.originalEvent.targetTouches[0].clientY,
                                    (e.originalEvent.targetTouches[0].clientX+77)>$(window).width()?(e.originalEvent.targetTouches[0].clientX-77):e.originalEvent.targetTouches[0].clientX];
                                menuPop(e,point)
                            }, 1000);
                        },
                        touchmove: function() {
                            clearTimeout(timeOutEvent);
                            timeOutEvent = 0;
                        }
                    });
                    ele.on('click',function (e) {
                        clearTimeout(timeOutEvent);
                        timeOutEvent = 0;
                    })
                }
            }
        }
    );

    table.on('toolbar(table)', function (obj) {
        var checkStatus = table.checkStatus(obj.config.id);
        var data = [];
        for (var i = 0; i < checkStatus.data.length; i++) {
            data.push(checkStatus.data[i].id);
        }
        switch (obj.event) {
            case 'createFolder':
                layer.prompt({
                    formType: 0,
                    value: '新建文件夹',
                    title: '文件夹名称'
                }, function(value, index, elem){
                    $.get('/folder/create',{parentId:folderId,title:value},function (res) {
                        if (res.code===200){
                            tableIns.reload();

                        }
                    });
                    layer.close(index);
                });
        }
    });

    table.on('row(table)', function(obj){
        if (obj.data.type===0)
            window.location.href='/myFiles?folderId='+obj.data.id;
        else if (getFileType(obj.data.name)==='MP4'){
            layer.open({
                type:1,
                content:'<video controls preload="auto" width="100%" height="'+($(window).height()-20)+'px">' +
                        '<source  src="/file/'+obj.data.id+'/'+obj.data.name+'" type=\'video/mp4; codecs="avc1.42E01E, mp4a.40.2"\'>'+
                        '</video>',
                move: false,
                shadeClose:true,
                shade: 0.001,
                resize:false,
                title: false,
                area:$(window).width()-20+'px'
            })
        } else if (getFileType(obj.data.name)==='DOC'||getFileType(obj.data.name)==='DOCX'||getFileType(obj.data.name)==='PDF'){
            window.open('/file/'+obj.data.id+'/'+obj.data.name+'?preview=true');
            // newTab('/file/'+obj.data.id+'?preview=true',obj.data.name)
        } else {
            var $eleForm = $("<form method='get'></form>");
            $eleForm.attr("action",window.location.href.split('/')[0]+'/file/'+obj.data.id+"/"+obj.data.name);
            $(document.body).append($eleForm);
            $eleForm.submit();
        }
    });

    function newTab(url,tit){
        if(top.layui.index){
            top.layui.index.openTabsPage(url,tit)
        }else{
            window.open(url)
        }
    }

    function menuPop(e,point) {

        var tr=$(e.target).parents('tr');
//        var tr = $('.layui-table-hover');
        var contextMenu;
        var id = $(tr).children('td').eq(0).children().text();
        var name = $(tr).children('td').eq(1).attr('data-content');
        var type = $(tr).children('td').eq(3).attr('data-content');
        var shared = $(tr).children('td').eq(5).children();
        var share = shared.text() === "true" ? "取消共享" : "共享";

        var dlOpen = '<dl style="margin: 8px;line-height: 36px;white-space: nowrap;border-radius: 2px">\n',
            dlClose = '</dl>',
            ddOpenFolder = '<dd style="cursor: pointer;font-size: 14px;"><a href="/myFiles?folderId=' + id + '"><img class="layui-icon" src="/assets/layui/images/folder-open.svg"> <b>打开</b></a></dd>\n',
            ddFileDownload = '<dd style="cursor: pointer;font-size: 14px;"><a href="/file/' + id + '/' + name + '"><img class="layui-icon" src="/assets/layui/images/download.svg"> <b>下载</b></a></dd>\n',
            ddFolderDownload = '<dd style="cursor: pointer;font-size: 14px;"><a href="/folder/' + id + '"><img class="layui-icon" src="/assets/layui/images/download.svg"> <b>下载</b></a></dd>\n',
            ddRename = '<dd style="cursor: pointer;font-size: 14px;"><a id="rename"><img class="layui-icon" src="/assets/layui/images/rename.svg"> 重命名</a></dd>\n',
            ddDelete = '<dd style="cursor: pointer;font-size: 14px;"><a id="delete"><img class="layui-icon" src="/assets/layui/images/delete.svg"> 删除</a></dd>\n',
            ddShare = '<dd style="cursor: pointer;font-size: 14px;"><a id="shareFile"><img class="layui-icon" src="/assets/layui/images/share.svg"> ' + share + '</a></dd>\n',
            ddCopy = '<dd style="cursor: pointer;font-size: 14px;"><a id="copyFile"><img class="layui-icon" src="/assets/layui/images/copy.svg"> 复制</a></dd>\n',
            ddMove = '<dd style="cursor: pointer;font-size: 14px;"><a id="moveFile"><img class="layui-icon" src="/assets/layui/images/move.svg"> 移动</a></dd>\n';

        if (type === '0')
            contextMenu = dlOpen + ddFolderDownload + ddOpenFolder + ddRename + ddDelete + dlClose;
        else
            contextMenu = dlOpen + ddFileDownload +ddCopy+ddMove+ ddShare + ddRename + ddDelete + dlClose;


        var menu = layer.open({
            type: 1,
            content: contextMenu,
            offset: point,
            move: false,
            shadeClose: true,
            shade: 0.001,
            closeBtn: 0,
            resize: false,
            title: false
        });
        $('#delete').on('click', function () {
            layer.close(menu);
            layer.confirm('确认删除吗？', function (index) {
                if (type !== '0') {
                    $.get('/file/delete', {id: id}, function (res) {
                        if (res.code === 200) {
                            $(tr).remove();
                            if ($('.layui-table').children('tbody').children().length === 0)
                                $('.layui-table-main').append('<div class="layui-none">无数据</div>');
                        } else
                            layer.msg(res.msg);
                    });
                } else
                    $.get('/folder/del', {id: id}, function (res) {
                        if (res.code === 200) {
                            $(tr).remove();
                            if ($('.layui-table').children('tbody').children().length === 0)
                                $('.layui-table-main').append('<div class="layui-none">无数据</div>');
                        } else
                            layer.msg(res.msg);
                    });
                layer.close(index);
            });
        });
        $('#rename').on('click', function () {
            if (type !== '0') {
                var index1 = name.lastIndexOf('.');
                name = name.substring(0, index1);
            }
            layer.close(menu);
            layer.prompt({
                formType: 0,
                title: '重命名',
                value: name
            }, function (value, index, elem) {
                if (type === '0') {
                    $.get('/folder/edit', {id: id, title: value}, function (res) {
                        if (res.code === 200) {
                            tableIns.reload();
                        }else {
                            layer.alert(res.msg,{icon:2})
                        }
                    });
                } else
                    $.get('/file/rename', {id: id, name: value}, function (res) {
                        if (res.code === 200) {
                            tableIns.reload();
                        }else {
                            layer.alert(res.msg,{icon:2})
                        }
                    });
                layer.close(index);
            });
        });
        $('#shareFile').on('click', function () {
            layer.close(menu);
            if (shared.text() === "false") {
                $.get('/file/share', {id: id}, function (res) {
                    if (res.code === 200) {
                        shared.text('true');
                        layer.msg('分享成功！');
                    }
                })
            } else {
                $.get('/file/unshared', {id: id}, function (res) {
                    if (res.code === 200) {
                        shared.text('false');
                        layer.msg('已取消分享');
                    }
                })
            }
        });
        $('#copyFile').on('click',function () {
            layer.close(menu);
            layer.open({
                type: 2,
                title: '复制文件',
                shadeClose: true,
                shade: 0.8,
                content: '/myFolderList?fileId=' + id+'&action=copy'
            });
        });
        $('#moveFile').on('click',function () {
            layer.close(menu);
            layer.open({
                type: 2,
                title: '移动文件',
                shadeClose: true,
                shade: 0.8,
                content: '/myFolderList?fileId=' + id+'&action=move'
            });
        })
    }
});

/**
 * @return {boolean}
 */
function GetQueryString(name){
    var reg=eval("/"+name+"/g");
    var r = window.location.search.substr(1);
    return reg.test(r);
}

function formatDate(fmt,date)
{
    var o = {
        "M+" : date.getMonth()+1,                 //月份
        "d+" : date.getDate(),                    //日
        "h+" : date.getHours(),                   //小时
        "m+" : date.getMinutes(),                 //分
        "s+" : date.getSeconds(),                 //秒
        "q+" : Math.floor((date.getMonth()+3)/3), //季度
        "S"  : date.getMilliseconds()             //毫秒
    };
    if(/(y+)/.test(fmt))
        fmt=fmt.replace(RegExp.$1, (date.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)
        if(new RegExp("("+ k +")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length===1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
    return fmt;
}

function formatSize(size) {
    var kb=(size/1024).toFixed(2);
    if (kb<1024)
        return kb+' KB';
    else
        return (size/1024/1024).toFixed(2)+' MB';
}

function getFileType(name) {
    var index1=name.lastIndexOf('.');
    var index2=name.length;
    var type= name.substring(index1+1,index2).toUpperCase();
    if (type==='GZ')
        type='TAR';
    return type;
}

var os = function() {
    var ua = navigator.userAgent,
        isWindowsPhone = /(?:Windows Phone)/.test(ua),
        isSymbian = /(?:SymbianOS)/.test(ua) || isWindowsPhone,
        isAndroid = /(?:Android)/.test(ua),
        isFireFox = /(?:Firefox)/.test(ua),
        isChrome = /(?:Chrome|CriOS)/.test(ua),
        isTablet = /(?:iPad|PlayBook)/.test(ua) || (isAndroid && !/(?:Mobile)/.test(ua)) || (isFireFox && /(?:Tablet)/.test(ua)),
        isPhone = /(?:iPhone)/.test(ua) && !isTablet,
        isPc = !isPhone && !isAndroid && !isSymbian;
    return {
        isTablet: isTablet,
        isPhone: isPhone,
        isAndroid : isAndroid,
        isPc : isPc
    };
}();