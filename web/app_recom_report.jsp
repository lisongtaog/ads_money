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
    <title>Ads Money | App Recommend Report</title>
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
        HashMap<String, String> countryMap = Utils.getCountryMap();
    %>

    <header class="main-header">

        <!-- Logo -->
        <a href="index.jsp" class="logo">
            <!-- mini logo for sidebar mini 50x50 pixels -->
            <span class="logo-mini"><b>Money</b></span>
            <!-- logo for regular state and mobile devices -->
            <span class="logo-lg">Ads <b>Recommend Money</b></span>
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
                <li class="active">
                    <a href="#">
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
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                App Recommend Report
            </h1>
            <ol class="breadcrumb">
                <li><a href="index.jsp"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">App Recommend Report</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="box box-default">
                <!-- /.box-header -->
                <div class="box-body">
                    <div class="row">
                        <div class="col-md-3">
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
                        <!-- /.col -->
                        <div class="col-md-5">
                            <div class="form-group">
                                <label>Filter</label>
                                <select  id="filter" class="form-control select2 select2-hidden-accessible" multiple="" data-placeholder="Select host app" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <%
                                        for (int i = 0; i < appDatas.size(); i++) {
                                            AppData one = appDatas.get(i);
                                    %>
                                    <option value="<%=one.appId%>"><%=one.appName%></option>
                                    <%
                                        }
                                    %>
                                </select>

                                <select  id="filter_target" class="form-control select2 select2-hidden-accessible" multiple="" data-placeholder="Select target app" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <%
                                        for (int i = 0; i < appDatas.size(); i++) {
                                            AppData one = appDatas.get(i);
                                    %>
                                    <option value="<%=one.appId%>"><%=one.appName%></option>
                                    <%
                                        }
                                    %>
                                </select>

                                <select  id="filterCountry" class="form-control select2 select2-hidden-accessible" multiple="" data-placeholder="Select country" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <%
                                        for (String countryCode : countryMap.keySet()) {
                                             String name = countryMap.get(countryCode);
                                    %>
                                    <option value="<%=countryCode%>"><%=name%></option>
                                    <%
                                        }
                                    %>
                                </select>
                            </div>
                        </div>
                        <!-- /.col -->

                        <div class="col-md-1">
                            <div class="form-group">
                                <label>&nbsp;</label>
                                <button id="btnQuery" type="button" class="btn btn-block btn-primary">Query</button>
                            </div>
                        </div>
                    </div>

                </div>
                <!-- /.box-body -->
            </div>
            <!-- /.row -->

            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header with-border">
                            <h3 class="box-title">Chart Report</h3>

                            <div class="box-tools pull-right">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i
                                        class="fa fa-minus"></i>
                                </button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i
                                        class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="text-center">
                                        <strong id="revenueChartTitle"></strong>
                                    </p>

                                    <div class="chart">
                                        <!-- Sales Chart Canvas -->
                                        <canvas id="revenueChart" style="height: 280px;"></canvas>
                                    </div>
                                    <!-- /.chart-responsive -->
                                </div>
                                <!-- /.col -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- ./box-body -->
                    </div>
                    <!-- /.box -->
                </div>
                <!-- /.col -->
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header with-border">
                            <h3 class="box-title">Firebase Data</h3>

                            <div class="box-tools pull-right">
                                <button type="button" class="btn btn-box-tool" data-widget="collapse"><i
                                        class="fa fa-minus"></i>
                                </button>
                                <button type="button" class="btn btn-box-tool" data-widget="remove"><i
                                        class="fa fa-times"></i></button>
                            </div>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="text-center">
                                        <strong id="firebaseChartTitle"></strong>
                                    </p>

                                    <div class="chart">
                                        <!-- Sales Chart Canvas -->
                                        <canvas id="firebaseChart" style="height: 280px;"></canvas>
                                    </div>
                                    <!-- /.chart-responsive -->
                                </div>
                                <!-- /.col -->
                            </div>
                            <!-- /.row -->
                        </div>
                        <!-- ./box-body -->
                    </div>
                    <!-- /.box -->
                </div>
                <!-- /.col -->
            </div>

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
                            <th>Host App</th>
                            <th>Country</th>
                            <th>Target App</th>
                            <th>Impression</th>
                            <th>Click</th>
                            <th>Installed</th>
                            <th>Revenue</th>
                            <th>ECPM</th>
                            <th>CTR</th>
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
                startDate: moment().subtract(6, 'days'),
                endDate  : moment()
            },
            function (start, end) {
                $('#reportrange span').html(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'))
            }
    );

    var start = moment().subtract(6, 'days');
    var end = moment();
    function setInitDate(start, end) {
        $('#reportrange span').html(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'))
    }
    setInitDate(start, end);

    $('#btnQuery').click(function() {
        queryData();
    });

    queryData();

    var firebaseChart;
    var revenueChart;

    function queryData() {
        var filter = $('#filter').val();
        var filterTarget = $('#filter_target').val();
        var filterCountry = $('#filterCountry').val();

        var drp = $('#reportrange').data('daterangepicker');
        var startDate = drp.startDate.format('YYYY-MM-DD');
        var endDate = drp.endDate.format('YYYY-MM-DD');

        var columns = [
            { data: 'date'},
            { data: 'app_name' },
            { data: 'country_name' },
            { data: 'target_app_name' },
            { data: 'ad_impression' },
            { data: 'ad_click' },
            { data: 'ad_installed' },
            { data: 'ad_revenue'},
            { data: 'ecpm'},
            { data: 'ctr'}
        ];
        if ($.fn.DataTable.isDataTable("#metricTable")) {
            $('#metricTable').DataTable().clear().destroy();
        }
        /*
        $('#metricTable th').remove();
        for (var i = 0; i < columns.length; i++) {
            var value = "";
            switch (columns[i].data) {
                case "date":
                    value = "Date";
                    break;
                case "app_name":
                    value = "App";
                    break;
                case "country_name":
                    value = "Country";
                    break;
                case "target_app_name":
                    value = "TargetApp";
                    break;
                case "ad_impression":
                    value = "Impression";
                    break;
                case "ad_click":
                    value = "Click";
                    break;
                case "ad_installed":
                    value = "Installed";
                    break;
                case "ad_revenue":
                    value = "Revenue";
                    break;
                case "ecpm":
                    value = "ECPM";
                    break;
                case "ctr":
                    value = "CTR";
                    break;
            }
            $('#metricTable thead tr').append($('<th>' + value + '</th>'));
        }
        */
        $('#metricTable').DataTable({
            "ordering": true,
            "processing": true,
            "serverSide": true,
            "searching": false,
            "pageLength": 15,
            "lengthMenu": [[10, 25, 50, 100, 500, 1000], [10, 25, 50, 100, 500, 1000]],
            "ajax": function (data, callback, settings) {
                var postData = {};
                postData.filter = filter.join(",");
                postData.filterTarget = filterTarget.join(",");
                postData.filterCountry = filterCountry.join(",");
                postData.start_date = startDate;
                postData.end_date = endDate;
                postData.page_index = data.start / data.length;
                postData.page_size = data.length;
                postData.order = data.order[0].column + (data.order[0].dir == 'asc' ? 1000 : 0);

                $.post("recommendReport/query", postData, function (data) {
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

        $.post("/recommendReport/get", {
            filter: filter.join(","),
            filterTarget:filterTarget.join(","),
            filterCountry: filterCountry.join(","),
            start_date: startDate,
            end_date: endDate
        }, function (data) {
            if (data && data.ret == 1) {
                var list = data.data;
                if (list.length > 0) {
                    var first = list[0];
                    var last = list[list.length - 1];
                    $('#revenueChartTitle').text(new Date(last.date).toLocaleDateString()  + " - " + new Date(first.date).toLocaleDateString());

                    var maps = {};
                    var labels = [];
                    var adRequest = [];
                    var adFilled = [];
                    var adImpression = [];
                    var adClick = [];
                    var adRevenue = [];
                    var adECPM = [];
                    for (var i = list.length - 1; i >= 0; i--) {
                        var one = list[i];
                        var date = new Date(one.date).toLocaleDateString();
                        var item = maps[date];
                        if (!item) {
                            labels.push(date);
                            item = {"ad_request": one.ad_request, "ad_filled": one.ad_filled, "ad_impression": one.ad_impression, "ad_click": one.ad_click, "ad_revenue": one.ad_revenue, "ecpm": one.ecpm};
                            maps[date] = item;
                        } else {
                            item.ad_request += one.ad_request;
                            item.ad_filled += one.ad_filled;
                            item.ad_impression += one.ad_impression;
                            item.ad_click += one.ad_click;
                            item.ad_revenue += one.ad_revenue;
                            item.ecpm = item.ad_impression > 0 ? item.ad_revenue / item.ad_impression * 1000 : 0;
                        }
                    }
                    for (var i = 0; i < labels.length; i++) {
                        var item = maps[labels[i]];
                        adRequest.push(item.ad_request);
                        adFilled.push(item.ad_filled);
                        adImpression.push(item.ad_impression);
                        adClick.push(item.ad_click);
                        adRevenue.push(item.ad_revenue);
                        adECPM.push(item.ecpm);
                    }
                    var chartConfig = {
                        'type': 'line',
                        'data' : {
                            'labels': labels,
                            'datasets': [
                                {
                                    label               : 'Request',
                                    borderColor         : '#00c0ef',
                                    fill: false,
                                    data                : adRequest
                                },
                                {
                                    label               : 'Filled',
                                    borderColor         : '#dd4b39',
                                    fill: false,
                                    data                : adFilled
                                },
                                {
                                    label               : 'Impression',
                                    borderColor         : '#00a65a',
                                    fill: false,
                                    data                : adImpression
                                },
                                {
                                    label               : 'Click',
                                    borderColor         : '#f39c12',
                                    fill: false,
                                    data                : adClick
                                },
                                {
                                    label               : 'Revenue',
                                    borderColor         : '#0073b7',
                                    fill: false,
                                    data                : adRevenue
                                },
                                {
                                    label               : 'ECPM',
                                    borderColor         : '#e842f4',
                                    fill: false,
                                    data                : adECPM
                                },
                            ],
                        },
                        options: {
                            scaleShowGridLines      : true,
                            scaleGridLineWidth      : 1,
                            legend : {
                                position: 'bottom'
                            }
                        }
                    };

                    var revenueChartCanvas = $('#revenueChart').get(0).getContext('2d');
                    // This will get the first returned node in the jQuery collection.
                    if (revenueChart) {
                        revenueChart.data = chartConfig.data;
                        revenueChart.update();
                    } else {
                        revenueChart = new Chart(revenueChartCanvas, chartConfig);
                    }
                }
            } else {
            }
        }, "json");

        $.post("/recommendReport/getFirebase", {
            filter: filter.join(","),
            filterCountry: filterCountry.join(","),
            start_date: startDate,
            end_date: endDate
        }, function (data) {
            if (data && data.ret == 1) {
                var list = data.data;
                if (list.length > 0) {
                    var first = list[0];
                    var last = list[list.length - 1];
                    $('#firebaseChartTitle').text(new Date(last.date).toLocaleDateString()  + " - " + new Date(first.date).toLocaleDateString());

                    var maps = {};
                    var labels = [];
                    var totalUser = [];
                    var activeUser = [];
                    var installed = [];
                    var uninstalled = [];
                    var todayUninstalled = [];
                    for (var i = list.length - 1; i >= 0; i--) {
                        var one = list[i];
                        var date = new Date(one.date).toLocaleDateString();
                        var item = maps[date];
                        if (!item) {
                            labels.push(date);
                            item = {"total_user": one.total_user, "active_user": one.active_user, "installed": one.installed, "uninstalled": one.uninstalled, "today_uninstalled": one.today_uninstalled};
                            maps[date] = item;
                        } else {
                            item.total_user += one.total_user;
                            item.active_user += one.active_user;
                            item.installed += one.installed;
                            item.uninstalled += one.uninstalled;
                            item.today_uninstalled += one.today_uninstalled;
                        }
                    }
                    for (var i = 0; i < labels.length; i++) {
                        var item = maps[labels[i]];
                        totalUser.push(item.total_user);
                        activeUser.push(item.active_user);
                        installed.push(item.installed);
                        uninstalled.push(item.uninstalled);
                        todayUninstalled.push(item.today_uninstalled);
                    }
                    var chartConfig = {
                        'type': 'line',
                        'data' : {
                            'labels': labels,
                            'datasets': [
                                {
                                    label               : 'TotalUser',
                                    borderColor         : '#00c0ef',
                                    fill: false,
                                    data                : totalUser
                                },
                                {
                                    label               : 'ActiveUser',
                                    borderColor         : '#dd4b39',
                                    fill: false,
                                    data                : activeUser
                                },
                                {
                                    label               : 'Installed',
                                    borderColor         : '#00a65a',
                                    fill: false,
                                    data                : installed
                                },
                                {
                                    label               : 'Uninstalled',
                                    borderColor         : '#f39c12',
                                    fill: false,
                                    data                : uninstalled
                                },
                                {
                                    label               : 'TodayUninstalled',
                                    borderColor         : '#0073b7',
                                    fill: false,
                                    data                : todayUninstalled
                                },
                            ],
                        },
                        options: {
                            scaleShowGridLines      : true,
                            scaleGridLineWidth      : 1,
                            legend : {
                                position: 'bottom'
                            }
                        }
                    };

                    var firebaseChartCanvas = $('#firebaseChart').get(0).getContext('2d');
                    // This will get the first returned node in the jQuery collection.
                    if (firebaseChart) {
                        firebaseChart.data = chartConfig.data;
                        firebaseChart.update();
                    } else {
                        firebaseChart = new Chart(firebaseChartCanvas, chartConfig);
                    }
                }
            } else {
            }
        }, "json");
    }
</script>
</body>
</html>