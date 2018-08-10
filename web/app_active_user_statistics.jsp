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
    <title>Ads Money | App User Analysis</title>
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
    <style>
        table.summary tr td{
            border-collapse: collapse;
            /*border: 1px solid #aac5b7;*/
        }
        table.summary tr td:nth-child(1n){
            text-align: right;
        }
        table.summary tr td:nth-child(2n){
            padding: 5px;
        }
    </style>
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
                App User Analysis
            </h1>
            <ol class="breadcrumb">
                <li><a href="index.jsp"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">App User Analysis</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="box box-default">
                <!-- /.box-header -->
                <div class="box-body">
                    <div class="row">
                        <!-- /.col -->
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Install Date:</label>
                                <div class="input-group date">
                                    <div class="input-group-addon">
                                        <i class="fa fa-calendar"></i>
                                    </div>
                                    <input type="text" class="form-control pull-right" id="txtInstallDate">
                                </div>
                                <span style="color: red">只能查询6.27日到前天的数据；当晚23点后可查询昨天的数据</span>
                            </div>
                        </div>

                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Filter</label>
                                <select  id="filter" class="form-control select2 select2-hidden-accessible" data-placeholder="Select app" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <%
                                        for (int i = 0; i < appDatas.size(); i++) {
                                            AppData one = appDatas.get(i);
                                    %>
                                    <option value="<%=one.appId%>"><%=one.appName%></option>
                                    <%
                                        }
                                    %>
                                </select>

                                <select  id="filterCountry" class="form-control select2 select2-hidden-accessible" data-placeholder="Select country" style="width: 100%;" tabindex="-1" aria-hidden="true">
                                    <option value="all">全部</option>
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

            <div class="box box-default">
                <div class="box-header with-border">
                    <p style="color: coral">公式：</p>
                    <p>
                        <span>活跃占比=活跃用户数/firebase安装量&nbsp;;&nbsp;&nbsp;</span>
                        <span>首日活跃占比=当天活跃用户数/首日活跃用户数&nbsp;;&nbsp;&nbsp;</span>
                        <span>累计活跃占比=自安装日至当日的活跃用户数总和/firebase安装量&nbsp;;&nbsp;&nbsp;</span>
                        <span>当日广告展示数=firebase统计的自安装日起，每个活跃日期的广告展示次数&nbsp;;&nbsp;&nbsp;</span>
                        <span>人均广告展示次数=累计的总的广告展示次数/firebase安装量&nbsp;;&nbsp;&nbsp;</span>
                    </p>

                    <p>
                        <%-- adplatform变现ecpm 即money后台拉取的广告单元变现数据 的平均ecpm--%>
                        <span>当日ECPM = 当日变现收益 / 当日广告单元展示次数 * 1000&nbsp;;&nbsp;&nbsp;</span>
                        <span title="根据广告展示次数比例估算">当日收入=当日老用户变现收益 * (选定安装日期那天安装的用户所看广告展示次数/所有老用户的广告展示次数)&nbsp;;&nbsp;&nbsp;</span>
                        <span>累计收入= 自安装日至当日的收入总和&nbsp;;&nbsp;&nbsp;</span>
                        <span>ltv = 累计收入/firebase安装量&nbsp;;&nbsp;&nbsp;</span>
                        <span>回本率=ltv / cpa&nbsp;;&nbsp;&nbsp;</span>
                    </p>
                </div>
                <div>
                    <table class="summary">
                        <tr>
                            <td><label>安装日期：</label></td>           <td id="installDate"></td>
                            <td><label>firebase安装量：</label></td>     <td id="total_install">0</td>
                            <td><label>adPlatform购买量：</label></td>   <td id="purchase_install">0</td>
                            <%--<td><label>购买占比：</label></td>           <td id="purchase_per">0%</td>--%>
                            <td><label>adPlatform 花费：</label></td>    <td id="purchase_cost">0</td>
                            <td><label>CPA：</label></td>                <td id="purchase_cpa">0</td>
                            <td><label>app版本：</label></td>            <td id="app_version"></td>
                            <td><label>adPlatform首日收入：</label></td>           <td id="first_revenue">0</td>
                            <td><label>adPlatform首日广告展示次数：</label></td>   <td id="first_impression">0</td>
                            <td><label>adPlatform首日ECPM：</label></td>           <td id="first_ecpm">0</td>
                        </tr>
                    </table>
                </div>
                <!-- /.box-header -->
                <div class="box-body" style="overflow-x: hidden">
                    <table id="metricTable" class="table table-bordered table-hover" cellspacing="0" >
                        <thead>
                        <tr>
                            <th>活跃日期</th>
                            <th>活跃用户数</th>
                            <th>活跃占比%</th>
                            <th>首日活跃占比%</th>
                            <th>累计活跃占比%</th>
                            <th>当日广告展示数</th>
                            <th>人均累计<br/>广告展示数</th>
                            <th title="adPlatform">当日ECPM</th><%--adPlatform当日ECPM--%>
                            <%--<th class="bg-info">当日ECPM</th>--%>
                            <th class="bg-info">当日收入</th>
                            <th class="bg-info">累计收入</th>
                            <th class="bg-info">LTV</th>
                            <th class="bg-info">回本率%</th>
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
    $("li[role='menu_li']:eq(16)").addClass("active");
    $('.select2').select2();
    $('#txtInstallDate').datepicker({
        format: 'yyyy-mm-dd',
        startDate:"2018-06-27",
        endDate: new Date(),
        autoclose: true
    });
    $('#txtInstallDate').datepicker('setDate', moment().subtract(2, 'days').format('YYYY-MM-DD'));

    $('#btnQuery').click(function() {
        var date = moment($('#txtInstallDate').data('datepicker').dates[0]).format('YYYY-MM-DD');
        var filter = $('#filter').val();
        var filterCountry = $('#filterCountry').val();
        queryData(date,filter,filterCountry);
    });

    function queryData(date,filter,filterCountry) {
        reset();
        var columns = [
            { title: "活跃日期" },
            { title: "活跃用户数" },
            { title: "活跃占比%" },
            { title: "首日活跃占比%" },
            { title: "累计活跃占比%" },
            { title: "当日广告展示数" },
            { title: "人均累计<br/>广告展示数" },
            { title: "当日ECPM" },/* adPlatform当日ECPM */
            /*{ title: "当日ECPM" },*/
            { title: "当日收入" },
            { title: "累计收入" },
            { title: "LTV" },
            { title: "回本率%" }
        ];
        if ($.fn.DataTable.isDataTable("#metricTable")) {
            $('#metricTable').DataTable().clear().destroy();
        }

        $('#metricTable').DataTable({
            searching: false,
            paging: false,
            processing: true,
            ordering: false,
            columns: columns,
            columnDefs:[
                /*{
                 targets: [1,2,3],
                 visible: false
                 }*/
            ],
            "ajax": function (data, callback, settings) {
                $.post('query_app_active_user_statistics', {
                    date: date,
                    appId: filter,
                    countryCode: filterCountry
                }, function (result) {
                    if (result && result.ret == 1) {
                        var dataSet = result;//表格数据
                        renderTable(dataSet);//填充表头数据
                        var data = dataSet.dataArray;
                        callback(
                            {
                                "recordsTotal": data.length,
                                "recordsFiltered": data.length,
                                "data": data
                            }
                        );
                    } else {
                        alert(result.message);
                    }
                }, 'json');
            }
        });
    }

    function reset() {
        $("#installDate").text("");
        $("#total_install").text(0);
        $("#purchase_install").text(0);
        //$("#purchase_per").text(0 +"%");
        $("#purchase_cost").text(0);
        $("#purchase_cpa").text(0);

        $("#app_version").text("");
        $("#first_revenue").text(0);
        $("#first_impression").text(0);
        $("#first_ecpm").text(0);
        $('#metricTable').DataTable().clear().destroy();
    }

    function  renderTable(dataSet) {
        if(dataSet){
            var summary = dataSet.summary;//表头汇总数据
            $("#installDate").text(summary.installDate );
            $("#total_install").text(summary.totalInstall );
            $("#purchase_install").text(summary.purchaseInstall );
            //$("#purchase_per").text(summary.purchasePer +"%");
            $("#purchase_cost").text(summary.purchaseCost);
            $("#purchase_cpa").text(summary.purchaseCpa);

            $("#app_version").text(summary.appVersion);
            $("#first_revenue").text(summary.firstRevenue);
            $("#first_impression").text(summary.firstImpression);
            $("#first_ecpm").text(summary.firstEcpm);

        }
    }

</script>
</body>
</html>