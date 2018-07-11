<%@ page import="com.bestgo.adsmoney.servlet.AdMobAccount" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppAdMobAccount" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | Firebase Project Management</title>
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
    %>
    <%@include file="common/main_sidebar.jsp"%>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <div class="box">
            <div class="box-body">
                <table id="appTable" class="table table-bordered table-hover" cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th>ProjectId</th>
                        <th>ProjectName</th>
                        <th>PropertyId</th>
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
    $("li[role='menu_li']:eq(2)").addClass("active");
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
                            url:  'firebase_management/create',
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
                            url:  'firebase_management/update',
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
                            url:  'firebase_management/delete',
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
            "label": "ProjectId:",
            "name": "project_id"
        }, {
            "label": "ProjectName:",
            "name": "project_name"
        }, {
            "label": "PropertyId:",
            "name": "property_id"
        },
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
            $.post("firebase_management/query", postData, function (data) {
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
            { data: 'project_id' },
            { data: 'project_name' },
            { data: 'property_id' },
            // etc
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