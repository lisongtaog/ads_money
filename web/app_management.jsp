<%@ page import="com.bestgo.adsmoney.servlet.AdMobAccount" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppAdMobAccount" %>
<%@ page import="com.bestgo.adsmoney.bean.FirebaseProject" %>
<%@ page import="com.bestgo.adsmoney.servlet.FirebaseManagement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | App Management</title>
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

        List<AppAdMobAccount> accounts = AdMobAccount.fetchAllAccounts();
        List<FirebaseProject> firebaseProjects = FirebaseManagement.fetchAllFirebaseProject();
    %>
    <header class="main-header">

        <!-- Logo -->
        <a href="index.jsp" class="logo">
            <!-- mini logo for sidebar mini 50x50 pixels -->
            <span class="logo-mini"><b>Money</b></span>
            <!-- logo for regular state and mobile devices -->
            <span class="logo-lg">Ads <b>Money</b></span>
        </a>

        <!-- Header Navbar: style can be found in header.less -->
        <nav class="navbar navbar-static-top">
            <!-- Sidebar toggle button-->
            <a href="#" class="sidebar-toggle" data-toggle="push-menu" role="button">
                <span class="sr-only">Toggle navigation</span>
            </a>
            <!-- Navbar Right Menu -->
        </nav>
    </header>
    <!-- Left side column. contains the logo and sidebar -->
    <aside class="main-sidebar">
        <!-- sidebar: style can be found in sidebar.less -->
        <section class="sidebar">
            <!-- sidebar menu: : style can be found in sidebar.less -->
            <ul class="sidebar-menu" data-widget="tree">
                <li class="header">NAVIGATION</li>
                <li class="">
                    <a href="index.jsp">
                        <i class="fa fa-dashboard"></i> <span>Dashboard</span>
                    </a>
                </li>
                <li class="active">
                    <a href="#">
                        <i class="fa fa-files-o"></i>
                        <span>App Management</span>
                    </a>
                </li>
                <li class="">
                    <a href="firebase_management.jsp">
                        <i class="fa fa-book"></i>
                        <span>Firebase Management</span>
                    </a>
                </li>
                <li class="">
                    <a href="admob_account_management.jsp">
                        <i class="fa fa-th"></i>
                        <span>AdMob Account Management</span>
                    </a>
                </li>
                <li class="">
                    <a href="ad_unit_management.jsp">
                        <i class="fa fa-list-alt"></i>
                        <span>Ad Unit Management</span>
                    </a>
                </li>
                <li class="">
                    <a href="user_defined_sql.jsp">
                        <i class="fa fa-scribd"></i>
                        <span>User Defined SQL</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_report.jsp">
                        <i class="fa fa-folder"></i>
                        <span>App Report</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_recom_report.jsp">
                        <i class="fa fa-folder"></i>
                        <span>App Recommend Report</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_trend.jsp">
                        <i class="fa fa-superpowers"></i>
                        <span>App Trend</span>
                    </a>
                </li>
                <li class="">
                    <a href="country_report.jsp">
                        <i class="fa fa-free-code-camp"></i>
                        <span>Country Report</span>
                    </a>
                </li>
                <li class="">
                    <a href="ctr_monitor.jsp">
                        <i class="fa fa-check"></i>
                        <span>CTR Monitor</span>
                    </a>
                </li>
                <li class="">
                    <a href="ad_impression_monitor.jsp">
                        <i class="fa fa-snowflake-o"></i>
                        <span>Ad Impression Monitor</span>
                    </a>
                </li>
                <li class="">
                    <a href="active_user_ad_chance_report.jsp">
                        <i class="fa fa-microchip"></i>
                        <span>Active User Ad Chance</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_hourly_trend.jsp">
                        <i class="fa fa-quora"></i>
                        <span>App Hourly Trend</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_active_user_statistics.jsp">
                        <i class="fa fa-folder"></i>
                        <span>App Active User Statistics</span>
                    </a>
                </li>
                <li class="">
                    <a href="app_ads_impressions_statistics.jsp">
                        <i class="fa fa-list-alt"></i>
                        <span>App Ads Impressions Statistics</span>
                    </a>
                </li>
            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <div class="box">
            <div class="box-body">
                <table id="appTable" class="table table-bordered table-hover" cellspacing="0" width="100%">
                    <thead>
                    <tr>
                        <th>AppId</th>
                        <th>AppName</th>
                        <th>FB AccessToken</th>
                        <th>FB AppId</th>
                        <th>AdMob Account</th>
                        <th>Firebase Project</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
    <!-- /.content-wrapper -->

    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0
        </div>
        <strong>Copyright &copy; 2012-2017 <a href="#">Think Mobile</a>.</strong> All rights
        reserved.
    </footer>
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
                            url:  'app_management/create',
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
                            url:  'app_management/update',
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
                            url:  'app_management/delete',
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
            "name": "app_id"
        }, {
            "label": "App Name:",
            "name": "app_name"
        }, {
            "label": "FB Access Token:",
            "name": "fb_access_token"
        }, {
            "label": "FB App Id:",
            "name": "fb_app_id"
        }, {
            "label": "AdMob Account:",
            "name": "admob_account",
            "type": "select",
            "options": [
                    <%
                    for (int i = 0; i < accounts.size(); i++) {
                    %>
                    "<%=accounts.get(i).account%>",
                    <% } %>
            ]
        }, {
            "label": "Firebase Project:",
            "name": "firebase_project_id",
            "type": "select",
            "options": [
                <%
                for (int i = 0; i < firebaseProjects.size(); i++) {
                %>
                { label: "<%=firebaseProjects.get(i).projectName%>", value: "<%=firebaseProjects.get(i).projectId%>" },
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
            $.post("app_management/query", postData, function (data) {
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
            { data: 'app_name' },
            { data: 'fb_access_token' },
            { data: 'fb_app_id' },
            { data: 'admob_account' },
            { data: 'firebase_project_id' },
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