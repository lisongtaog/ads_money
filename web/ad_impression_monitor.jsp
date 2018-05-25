<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppData" %>
<%@ page import="com.bestgo.adsmoney.servlet.AppManagement" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.bestgo.adsmoney.Utils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | Impression Monitor</title>
    <link rel="shortcut icon" href="/images/favicon.ico">

    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <!-- Bootstrap 3.3.7 -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/bootstrap/dist/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <!-- Ionicons -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/Ionicons/css/ionicons.min.css">
    <!-- jvectormap -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/jvectormap/jquery-jvectormap.css">
    <!-- daterange picker -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
    <!-- bootstrap datepicker -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css">
    <!-- Select2 -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/select2/dist/css/select2.min.css">
    <!-- DataTables -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.4.1/css/buttons.dataTables.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/select/1.2.2/css/select.dataTables.min.css">

    <!-- Theme style -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/dist/css/AdminLTE.min.css">
    <!-- AdminLTE Skins. Choose a skin from the css/skins
         folder instead of downloading all of them to reduce the load. -->
    <link rel="stylesheet" href="http://money.uugame.info/admin_lte/dist/css/skins/_all-skins.min.css">

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

        List<AppData> appDatas = AppManagement.fetchAllAppData();
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
                <li class="">
                    <a href="app_management.jsp">
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
                <li class="active">
                    <a href="#">
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
                        <span>App Hourly Tread</span>
                    </a>
                </li>
            </ul>
        </section>
        <!-- /.sidebar -->
    </aside>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                Impression Monitor
            </h1>
            <ol class="breadcrumb">
                <li><a href="index.jsp"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">Impression Monitor</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="box box-default">
                <!-- /.box-header -->
                <div class="box-body">
                    <div class="row">
                        <!-- /.col -->
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Filter</label>
                                <select  id="filter" class="form-control select2 select2-hidden-accessible" multiple="" data-placeholder="Select app" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <%
                                        for (int i = 0; i < appDatas.size(); i++) {
                                            AppData one = appDatas.get(i);
                                    %>
                                    <option value="<%=one.appId%>"><%=one.appName%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </div>
                        </div>
                        <!-- /.col -->

                        <div class="col-md-4">
                            <!-- Date and time range -->
                            <div class="form-group">
                                <label>Date range:</label>

                                <div class="input-group">
                                    <div id="reportrange" class="pull-right" style="background: #fff; cursor: pointer; padding: 5px 10px; border: 1px solid #ccc; width: 100%">
                                        <i class="glyphicon glyphicon-calendar fa fa-calendar"></i>&nbsp;
                                        <span></span> <b class="caret"></b>
                                    </div>
                                </div>

                            </div>
                            <!-- /.form group -->
                        </div>

                        <div class="col-md-2">
                            <div class="form-group">
                                <label>&nbsp;</label>
                                <button id="btnQuery" type="button" class="btn btn-block btn-primary">Query</button>
                            </div>
                        </div>

                        <!-- /.col -->
                    </div>
                    <!-- /.row -->

                </div>
                <!-- /.box-body -->
            </div>
            <!-- /.row -->

            <div class="box box-default">
                <div class="box-header with-border">
                    <h3 class="box-title">Metrics</h3>
                </div>
                <!-- /.box-header -->
                <div class="box-body" style="overflow-x: auto">
                    <table id="metricTable" class="table table-bordered table-hover" cellspacing="0" width="100%">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>AppName</th>
                            <th>TotalUser</th>
                            <th>ActiveUser</th>
                            <th title="活跃用户除以总用户">ActiveUserRate</th>
                            <th title="显示过广告的用户除以活跃用户">NonAdUserRate</th>
                            <th title="显示过全屏的用户除以活跃用户">FullAdUserRate</th>
                            <th title="全屏总数">FullAdCount</th>
                            <th title="显示过1次全屏的用户除以活跃用户">OneFullAdUserRate</th>
                            <th>TwoFullAdUserRate</th>
                            <th>ThreeFullAdUserRate</th>
                            <th>FourFullAdUserRate</th>
                            <th title="显示过5次以上全屏的用户除以活跃用户">FiveFullAdUserRate</th>
                            <th>NativeAdUserRate</th>
                            <th>NativeAdCount</th>
                            <th>OneNativeAdUserRate</th>
                            <th>TwoNativeAdUserRate</th>
                            <th>ThreeNativeAdUserRate</th>
                            <th>FourNativeAdUserRate</th>
                            <th>FiveNativeAdUserRate</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>

        </section>
        <!-- /.content -->
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
<!-- FastClick -->
<script src="http://money.uugame.info/admin_lte/bower_components/fastclick/lib/fastclick.js"></script>
<!-- AdminLTE App -->
<script src="http://money.uugame.info/admin_lte/dist/js/adminlte.min.js"></script>
<!-- Sparkline -->
<script src="http://money.uugame.info/admin_lte/bower_components/jquery-sparkline/dist/jquery.sparkline.min.js"></script>
<!-- jvectormap  -->
<script src="http://money.uugame.info/admin_lte/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
<script src="http://money.uugame.info/admin_lte/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
<!-- SlimScroll -->
<script src="http://money.uugame.info/admin_lte/bower_components/jquery-slimscroll/jquery.slimscroll.min.js"></script>
<!-- ChartJS -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.6.0/Chart.min.js"></script>

<!-- Select2 -->
<script src="http://money.uugame.info/admin_lte/bower_components/select2/dist/js/select2.full.min.js"></script>

<!-- date-range-picker -->
<script src="http://money.uugame.info/admin_lte/bower_components/moment/min/moment.min.js"></script>
<script src="http://money.uugame.info/admin_lte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<!-- bootstrap datepicker -->
<script src="http://money.uugame.info/admin_lte/bower_components/bootstrap-datepicker/dist/js/bootstrap-datepicker.min.js"></script>
<!-- DataTables -->
<script src="http://money.uugame.info/admin_lte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="http://money.uugame.info/admin_lte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/buttons/1.4.1/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script>
<script type="text/javascript" src="http://money.uugame.info/admin_lte/plugins/Editor-1.6.5/js/dataTables.editor.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.4.2/js/buttons.html5.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.4.2/js/buttons.print.min.js"></script>

<script>
    $('.select2').select2();

    //Date range as a button
    $('#reportrange').daterangepicker(
            {
                ranges   : {
                    'Today'       : [moment(), moment()],
                    'Yesterday'   : [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                    'Last 7 Days' : [moment().subtract(6, 'days'), moment()],
                    'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                    'This Month'  : [moment().startOf('month'), moment().endOf('month')],
                    'Last Month'  : [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
                },
                startDate: moment().subtract(1, 'days'),
                endDate  : moment().subtract(1, 'days')
            },
            function (start, end) {
                $('#reportrange span').html(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'))
            }
    );

    var start = moment().subtract(1, 'days');
    var end = moment().subtract(1, 'days');
    function setInitDate(start, end) {
        $('#reportrange span').html(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'))
    }
    setInitDate(start, end);

    $('#btnQuery').click(function() {
        queryData();
    });

    queryData();

    function queryData() {
        var filter = $('#filter').val();
        var drp = $('#reportrange').data('daterangepicker');
        var startDate = drp.startDate.format('YYYY-MM-DD');
        var endDate = drp.endDate.format('YYYY-MM-DD');

        var columns = [
            { data: 'date' },
            { data: 'app_name' },
            { data: 'total_user' },
            { data: 'active_user' },
            { data: 'active_user_rate' },
            { data: 'non_ad_user_rate' },
            { data: 'full_ad_user_rate' },
            { data: 'full_ad_count' },
            { data: 'one_full_ad_user_rate' },
            { data: 'two_full_ad_user_rate' },
            { data: 'three_full_ad_user_rate' },
            { data: 'four_full_ad_user_rate' },
            { data: 'five_full_ad_user_rate' },
            { data: 'native_ad_user_rate' },
            { data: 'native_ad_count' },
            { data: 'one_native_ad_user_rate' },
            { data: 'two_native_ad_user_rate' },
            { data: 'three_native_ad_user_rate' },
            { data: 'four_native_ad_user_rate' },
            { data: 'five_native_ad_user_rate' },
        ];

        if ($.fn.DataTable.isDataTable("#metricTable")) {
            $('#metricTable').DataTable().clear().destroy();
        }

        $('#metricTable').DataTable({
            "ordering": true,
            "processing": true,
            "serverSide": true,
            "searching": false,
            "pageLength": 500,
            "lengthMenu": [[10, 25, 50, 100, 500, 1000], [10, 25, 50, 100, 500, 1000]],
            "ajax": function (data, callback, settings) {
                var postData = {};
                postData.filter = filter.join(",");
                postData.start_date = startDate;
                postData.end_date = endDate;
                postData.page_index = data.start / data.length;
                postData.page_size = data.length;
                postData.order = data.order[0].column + (data.order[0].dir == 'asc' ? 1000 : 0);
                $.post("ad_impression_monitor/query", postData, function (data) {
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
            columns: columns,
            select: true,
            dom: 'Blfrtip',
            buttons: [{
                extend: 'collection',
                text: 'Export',
                buttons: ['copy', 'excel', 'csv', 'pdf', 'print']
                }],
        });
    }
</script>
</body>
</html>