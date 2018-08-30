<%@ page import="java.util.List" %>
<%@ page import="com.bestgo.adsmoney.bean.AppData" %>
<%@ page import="com.bestgo.adsmoney.servlet.AppManagement" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.bestgo.adsmoney.utils.Utils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Ads Money | App Trend</title>
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

    <%@include file="common/main_sidebar.jsp"%>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                App Trend
            </h1>
            <ol class="breadcrumb">
                <li><a href="index.jsp"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">App Trend</li>
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
                                <label>End Date:</label>
                                <div class="input-group date">
                                    <div class="input-group-addon">
                                        <i class="fa fa-calendar"></i>
                                    </div>
                                    <input type="text" class="form-control pull-right" id="txtEndDate">
                                </div>
                            </div>
                        </div>
                        <!-- /.col -->

                        <div class="col-md-6">
                            <!-- Date and time range -->
                            <div class="form-group">
                                <label>Period:</label>
                                <select id="selPeriod" class="form-control">
                                    <option value="1">Daily</option>
                                    <option value="2">Weekly</option>
                                    <option value="3">Monthly</option>
                                </select>

                            </div>
                            <!-- /.form group -->
                        </div>
                        <!-- /.col -->
                    </div>
                    <!-- /.row -->

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

            <%--<div class="row">
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
            </div>--%>

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
                            <th>Cost</th>
                            <th>PurchasedUser</th>
                            <th>Installed</th>
                            <th>UnInstalled</th>
                            <th>UnInstalledRate</th>
                            <th>TotalUser</th>
                            <th>TotalUserTrend</th>
                            <th>ActiveUser</th>
                            <th>ActiveUserTrend</th>
                            <th>Revenue</th>
                            <th>RevenueTrend</th>
                            <th>ARPU*10000</th>
                            <th>ARPUTrend</th>
                            <th>CPA</th>
                            <th>ECPM</th>
                            <th>CPA/ECPM</th>
                            <th>AvgSumImpression</th>
                            <th>Incoming</th>
                            <th>LTV</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>

        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <%@include file="common/main_footer.jsp"%>

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
    $("li[role='menu_li']:eq(7)").addClass("active");
    $('.select2').select2();
    $('#txtEndDate').datepicker({
        format: 'yyyy-mm-dd',
        autoclose: true
    });
    $('#txtEndDate').datepicker('setDate', moment().subtract(0, 'days').format('YYYY-MM-DD'));

    $('#btnQuery').click(function() {
        queryData();
    });

//    queryData();

    var revenueChart;

    function queryData() {
        var date = moment($('#txtEndDate').data('datepicker').dates[0]).format('YYYY-MM-DD');
        var filter = $('#filter').val();
        var filterCountry = $('#filterCountry').val();
        var period = $('#selPeriod').val();

        var columns = [
            { data: 'date' },
            { data: 'cost' },
            { data: 'purchased_user' },
            { data: 'total_installed' },
            { data: 'total_uninstalled' },
            { data: 'uninstalled_rate' },
            { data: 'total_user' },
            { data: 'total_user_trend' },
            { data: 'active_user' },
            { data: 'active_user_trend' },
            { data: 'revenue' },
            { data: 'revenue_trend' },
            { data: 'arpu' },
            { data: 'arpu_trend' },
            { data: 'cpa' },
            { data: 'ecpm' },
            { data: 'cpa_div_ecpm' },
            { data: 'avg_sum_impression' },
            { data: 'incoming' },
            { data: 'estimated_revenue' },
        ];

        if ($.fn.DataTable.isDataTable("#metricTable")) {
            $('#metricTable').DataTable().clear().destroy();
        }

        $('#metricTable').DataTable({
            "ordering": false,
            "processing": true,
            "serverSide": true,
            "searching": false,
            "pageLength": 25,
            "lengthMenu": [[10, 25, 50, 100, 500, 1000], [10, 25, 50, 100, 500, 1000]],
            "ajax": function (data, callback, settings) {
                var postData = {};
                postData.filter = filter.join(",");
                postData.filterCountry = filterCountry.join(",");
                postData.end_date = date;
                postData.period = period;
                postData.page_index = data.start / data.length;
                postData.page_size = data.length;
                $.post("app_trend2/query", postData, function (data) {
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

/*        $.post("/app_trend/get", {
            filter: filter.join(","),
            filterCountry: filterCountry.join(","),
            period: period,
            end_date: date
        }, function (data) {
            if (data && data.ret == 1) {
                var list = data.data;
                if (list.length > 0) {
                    var first = list[0];
                    var last = list[list.length - 1];
                    $('#revenueChartTitle').text(new Date(last.date).toLocaleDateString()  + " - " + new Date(first.date).toLocaleDateString());

                    var labels = [];
                    var incoming = [];
                    var totalUser = [];
                    var totalUserTrend = [];
                    var activeUser = [];
                    var totalInstalled = [];
                    var revenue = [];
                    var ecpm = [];
                    var arpu = [];
                    var cpa = [];
                    for (var i = list.length - 1; i >= 0; i--) {
                        var one = list[i];
                        var date = new Date(one.date).toLocaleDateString();
                        labels.push(date);
                        totalUser.push(one.total_user);
                        totalUserTrend.push(one.total_user_trend);
                        activeUser.push(one.active_user);
                        totalInstalled.push(one.total_installed);
                        revenue.push(one.revenue);
                        ecpm.push(one.ecpm);
                        arpu.push(one.arpu);
                        cpa.push(one.cpa);
                        incoming.push(one.incoming);
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
                                    label               : 'Installed',
                                    borderColor         : '#dd4b39',
                                    fill: false,
                                    data                : totalInstalled
                                },
                                {
                                    label               : 'Revenue',
                                    borderColor         : '#00a65a',
                                    fill: false,
                                    data                : revenue
                                },
                                {
                                    label               : 'ECPM',
                                    borderColor         : '#f39c12',
                                    fill: false,
                                    data                : ecpm
                                },
                                {
                                    label               : 'ARPU',
                                    borderColor         : '#0073b7',
                                    fill: false,
                                    data                : arpu
                                },
                                {
                                    label               : 'CPA',
                                    borderColor         : '#e842f4',
                                    fill: false,
                                    data                : cpa
                                },
                                {
                                    label               : 'Incoming',
                                    borderColor         : '#0aff0a',
                                    fill: false,
                                    data                : incoming
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
        }, "json");*/
    }
</script>
</body>
</html>