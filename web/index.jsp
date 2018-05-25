<%@ page import="com.bestgo.adsmoney.servlet.Dashboard" %>
<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppAdsMetrics" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | Dashboard</title>
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

        List<AppAdsMetrics> metrics = Dashboard.fetchAdsMetrics();
        int adRequest = 0;
        int adFilled = 0;
        int adImpression = 0;
        int adClick = 0;
        double adRevenue = 0;
        for (int i = 0; i < metrics.size(); i++) {
            adRequest += metrics.get(i).adRequest;
            adFilled += metrics.get(i).adFilled;
            adImpression += metrics.get(i).adImpression;
            adClick += metrics.get(i).adClick;
            adRevenue += metrics.get(i).adRevenue;
        }
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
                <li class="active">
                    <a href="#">
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
                <li class="">
                    <a href="ad_impression_monitor.jsp">
                        <i class="fa fa-snowflake-o"></i>
                        <span>Ad Impression Monitor</span>
                    </a>
                </li>
                <%--<li class="">--%>
                    <%--<a href="daily_active_user.jsp">--%>
                        <%--<i class="fa fa-microchip"></i>--%>
                        <%--<span>Daily Active User(AntivirusV2 Only)</span>--%>
                    <%--</a>--%>
                <%--</li>--%>
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
                Dashboard
                <small>Version 1.0</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">Dashboard</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <!-- Info boxes -->
            <div class="row">
                <div class="col-md-3 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-aqua"><i class="ion ion-arrow-graph-up-right"></i></span>

                        <div class="info-box-content">
                            <span class="info-box-text">Request</span>
                            <span class="info-box-number"><%=adRequest%></span>
                        </div>
                        <!-- /.info-box-content -->
                    </div>
                    <!-- /.info-box -->
                </div>
                <!-- /.col -->
                <div class="col-md-3 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-red"><i class="ion ion-arrow-graph-up-right"></i></span>

                        <div class="info-box-content">
                            <span class="info-box-text">Filled</span>
                            <span class="info-box-number"><%=adFilled%></span>
                        </div>
                        <!-- /.info-box-content -->
                    </div>
                    <!-- /.info-box -->
                </div>
                <!-- /.col -->

                <!-- fix for small devices only -->
                <div class="clearfix visible-sm-block"></div>

                <div class="col-md-3 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-green"><i class="ion ion-arrow-graph-up-right"></i></span>

                        <div class="info-box-content">
                            <span class="info-box-text">Impression</span>
                            <span class="info-box-number"><%=adImpression%></span>
                        </div>
                        <!-- /.info-box-content -->
                    </div>
                    <!-- /.info-box -->
                </div>
                <!-- /.col -->
                <div class="col-md-3 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-yellow"><i class="ion ion-arrow-graph-up-right"></i></span>

                        <div class="info-box-content">
                            <span class="info-box-text">Click</span>
                            <span class="info-box-number"><%=adClick%></span>
                        </div>
                        <!-- /.info-box-content -->
                    </div>
                    <!-- /.info-box -->
                </div>

                <div class="col-md-3 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-blue"><i class="ion ion-arrow-graph-up-right"></i></span>

                        <div class="info-box-content">
                            <span class="info-box-text">Revenue</span>
                            <span class="info-box-number"><%=adRevenue%></span>
                        </div>
                        <!-- /.info-box-content -->
                    </div>
                    <!-- /.info-box -->
                </div>
                <!-- /.col -->
            </div>
            <!-- /.row -->

            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header with-border">
                            <h3 class="box-title">Monthly Report</h3>

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
            <!-- /.row -->
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

<script>
    $.post("dashboard", {
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
                for (var i = list.length - 1; i >= 0; i--) {
                    var one = list[i];
                    var date = new Date(one.date).toLocaleDateString();
                    var item = maps[date];
                    if (!item) {
                        labels.push(date);
                        item = {"ad_request": one.ad_request, "ad_filled": one.ad_filled, "ad_impression": one.ad_impression, "ad_click": one.ad_click, "ad_revenue": one.ad_revenue};
                        maps[date] = item;
                    } else {
                        item.ad_request += one.ad_request;
                        item.ad_filled += one.ad_filled;
                        item.ad_impression += one.ad_impression;
                        item.ad_click += one.ad_click;
                        item.ad_revenue += one.ad_revenue;
                    }
                }
                for (var i = 0; i < labels.length; i++) {
                    var item = maps[labels[i]];
                    adRequest.push(item.ad_request);
                    adFilled.push(item.ad_filled);
                    adImpression.push(item.ad_impression);
                    adClick.push(item.ad_click);
                    adRevenue.push(item.ad_revenue);
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
                var revenueChart = new Chart(revenueChartCanvas, chartConfig);
            }
        } else {
        }
    }, "json");
</script>
</body>
</html>