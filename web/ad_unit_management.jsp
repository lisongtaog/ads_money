<%@ page import="com.bestgo.adsmoney.servlet.AdMobAccount" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppAdMobAccount" %>
<%@ page import="com.bestgo.adsmoney.bean.AppData" %>
<%@ page import="com.bestgo.adsmoney.servlet.AppManagement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | Ad Unit Management</title>
    <link rel="shortcut icon" href="/images/favicon.ico">
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <!-- Bootstrap 3.3.7 -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/bootstrap/dist/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <!-- Ionicons -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/Ionicons/css/ionicons.min.css">
    <!-- DataTables -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.4.1/css/buttons.dataTables.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.4.1/css/buttons.dataTables.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/select/1.2.2/css/select.dataTables.min.css">
    <!-- Theme style -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/dist/css/AdminLTE.min.css">
    <!-- AdminLTE Skins. Choose a skin from the css/skins
         folder instead of downloading all of them to reduce the load. -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/dist/css/skins/_all-skins.min.css">
    <link rel="stylesheet" type="text/css" href="http://money.uugame.info/admin_lte/plugins/Editor-1.6.5/css/editor.dataTables.min.css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <!-- Google Font -->
    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,300italic,400italic,600italic">
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
    <%
        Object object = session.getAttribute("isAdmin");
        if (object == null) {
            response.sendRedirect("login.jsp");
        }

        List<AppData> appList = AppManagement.fetchAllAppData();

        List<AppAdMobAccount> adMobAccounts = AdMobAccount.fetchAllAccounts();
    %>
    <%@include file="common/main_sidebar.jsp"%>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <div class="box">
            <div class="box-body">
                <table id="appTable" class="table table-bordered table-hover" cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th>AppId</th>
                        <th>Network</th>
                        <th title="广告单元类型">Ad Unit Type</th>
                        <th title="广告单元ID">Ad Unit Id</th>
                        <th title="广告展示类型">Show Type</th>
                        <th title="新老用户标记">Flag</th>
                        <th title="广告单元名称">Ad Unit Name</th>
                        <th>AdMob Account</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
    <!-- /.content-wrapper -->

    <%@include file="common/main_footer.jsp"%>
</div>
<!-- ./wrapper -->

<!-- jQuery 3 -->
<script src="http://money.uugame.info/admin_lte/bower_components/jquery/dist/jquery.min.js"></script>
<!-- Bootstrap 3.3.7 -->
<script src="http://money.uugame.info/admin_lte/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>
<!-- DataTables -->
<script src="http://money.uugame.info/admin_lte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="http://money.uugame.info/admin_lte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/buttons/1.4.1/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script>
<script type="text/javascript" src="http://money.uugame.info/admin_lte/plugins/Editor-1.6.5/js/dataTables.editor.js"></script>

<!-- AdminLTE App -->
<script src="http://money.uugame.info/admin_lte/dist/js/adminlte.min.js"></script>

<script>
    $("li[role='menu_li']:eq(4)").addClass("active");
    var editor = new $.fn.dataTable.Editor( {
        "table": "#appTable",
        "ajax": function ( method, url, data, success, error ) {
            var action = data.action;
            var postData = null;
            var id = null;
            for (var key in data.data) {
                id = key;
                postData = data.data[key];
                postData.id = id;
                break;
            }
            if (!postData || !id) {
                error();
            } else {
                switch (action) {
                    case 'create':
                        $.ajax( {
                            type: 'POST',
                            url:  'ad_unit_management/create',
                            data: postData,
                            dataType: "json",
                            success: function (json) {
                                success( json );
                            },
                            error: function (xhr, error, thrown) {
                                error( xhr, error, thrown );
                            }
                        } );
                        break;
                    case 'edit':
                        $.ajax( {
                            type: 'POST',
                            url:  'ad_unit_management/update',
                            data: postData,
                            dataType: "json",
                            success: function (json) {
                                success( json );
                            },
                            error: function (xhr, error, thrown) {
                                error( xhr, error, thrown );
                            }
                        } );
                        break;
                    case 'remove':
                        $.ajax( {
                            type: 'POST',
                            url:  'ad_unit_management/delete',
                            data: postData,
                            dataType: "json",
                            success: function (json) {
                                if (json.ret == 1) {
                                    success({})
                                }
                            },
                            error: function (xhr, error, thrown) {
                                error( xhr, error, thrown );
                            }
                        } );
                        break;
                }
            }
            if (data.action == 'create') {

            } else if (data.action == 'edit') {

            }
        },
        "idSrc": "id",
        "fields": [ {
            "label": "AppId:",
            "name": "app_id",
            "type": "select",
            "options": [
                <%
                for (int i = 0; i < appList.size(); i++) {
                %>
                { label: "<%=appList.get(i).appName%>", value: "<%=appList.get(i).appId%>" },
                <% } %>
            ]
        }, {
            "label": "Network:",
            "name": "ad_network",
            "type": "select",
            "options": ["AdMob", "Facebook"],
        }, {
            "label": "Ad Unit Type:",
            "name": "ad_unit_type",
            "type": "select",
            "options": ["banner", "native", "interstitial"],
        }, {
            "label": "Ad Unit Id:",
            "name": "ad_unit_id"
        }, {
            "label": "Show Type:",
            "name": "show_type",
            "type": "select",
            "options": [
                {label:"未分类",value:"20"},
                {label:"Admob全屏高",value:"1"},{label:"Facebook全屏高",value:"2"},
                {label:"Admob全屏中",value:"3"},{label:"Facebook全屏中",value:"4"},
                {label:"Admob全屏低",value:"5"},{label:"Facebook全屏低",value:"6"},
                {label:"AdmobNative高",value:"7"},{label:"FacebookNative高",value:"8"},
                {label:"AdmobNative中",value:"9"},{label:"FacebookNative中",value:"10"},
                {label:"AdmobNative低",value:"11"},{label:"FacebookNative低",value:"12"}
            ]
        }, {
            "label": "Flag:",
            "name": "flag",
            "type": "select",
            "options": [{label:"常规",value:"0"},{label:"新安装",value:"1"},]
        },{
            "label": "Ad Unit Name:",
            "name": "ad_unit_name",
        }, {
            "label": "AdMob Account:",
            "name": "admob_account",
            "type": "select",
            "options": [
                { label: "和应用保持一致", value: "" },
                <%
                for (int i = 0; i < adMobAccounts.size(); i++) {
                %>
                { label: "<%=adMobAccounts.get(i).accountName%>", value: "<%=adMobAccounts.get(i).account%>" },
                <% } %>
            ]
        }
        ]
    } );

    $('#appTable').DataTable({
        "ordering": false,
        "processing": true,
        "serverSide": true,
        "ajax": function (data, callback, settings) {
            var postData = {};
            if (data.search && data.search.value) {
                postData.word = data.search.value;
            }
            postData.page_index = data.start / data.length;
            postData.page_size = data.length;
            $.post("ad_unit_management/query", postData, function (data) {
                if (data && data.ret == 1) {
                    var list = [];
                    for (var i = 0; i < data.data.length; i++) {
                        list.push(data.data[i]
                        );
                    }
                    callback(
                            {
                                "recordsTotal": data.total,
                                "recordsFiltered": data.total,
                                "data": list
                            }
                    );
                } else {
                    alert(data.msg);
                }
            }, "json");
        },
        dom: 'Bfrtip',
        columns: [
            { data: 'app_id' },
            { data: 'ad_network' },
            { data: 'ad_unit_type' },
            { data: 'ad_unit_id' },
            { data: 'show_type' },
            { data: 'flag' },
            { data: 'ad_unit_name' },
            { data: 'admob_account' },
            // etc
        ],"columnDefs":[
            {//第5列
                "targets":5,
                render: function(data, type, row) {
                    var html = "1" == row.flag ? "新安装": "常规";
                    return html;
                }
            },
            {//第4列
                "targets":4,
                render: function(data, type, row) {
                    var html = "";
                    switch (row.show_type) {
                        case "1":
                            html = "Admob全屏高";
                            break;
                        case "2":
                            html = "Facebook全屏高";
                            break;
                        case "3":
                            html = "Admob全屏中";
                            break;
                        case "4":
                            html = "Facebook全屏中";
                            break;
                        case "5":
                            html = "Admob全屏低";
                            break;
                        case "6":
                            html = "Facebook全屏低";
                            break;
                        case "7":
                            html = "AdmobNative高";
                            break;
                        case "8":
                            html = "FacebookNative高";
                            break;
                        case "9":
                            html = "AdmobNative中";
                            break;
                        case "10":
                            html = "FacebookNative中";
                            break;
                        case "11":
                            html = "AdmobNative低";
                            break;
                        case "12":
                            html = "FacebookNative低";
                            break;
                        default:
                            html = "未分类"
                    }
                    return html;
                }
            }
        ],
        select: true,
        buttons: [
            { extend: 'create', editor: editor },
            { extend: 'edit',   editor: editor },
            { extend: 'remove', editor: editor }
        ]
    });
</script>
</body>
</html>