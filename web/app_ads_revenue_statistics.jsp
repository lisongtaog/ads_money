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
    <title>Ads Money | App Ads Revenue Statistics</title>
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
                App Ads Revenue Statistics
            </h1>
            <ol class="breadcrumb">
                <li><a href="index.jsp"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">App Ads Revenue Statistics</li>
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
                                    <option value="all">全部</option>
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
            <div style="width:80%;height:70%" id="canvas_dev">
            </div>

            <div class="box box-default">
                <!-- /.box-header -->
                <div class="box-header with-border">
                    <span style="color: coral">公式：</span>购买占比=购买安装量/总安装量 &nbsp;;&nbsp;&nbsp; 购买累计收支比=购买安装累计收入/花费
                </div>

                <div class="box-body" style="overflow-x: hidden">
                    <table id="metricTable" class="table table-bordered table-hover" cellspacing="0" >
                        <thead>
                        <tr>
                            <th>安装日期</th>
                            <th>花费</th>
                            <th>总安装量</th>
                            <th>购买安装量</th>
                            <th>购买占比</th>
                            <th>统计日期</th>
                            <th>购买安装收入</th>
                            <th>总安装累计收入</th>
                            <th>购买安装累计收入</th>
                            <th>购买累计收支比</th>
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
    $("li[role='menu_li']:eq(14)").addClass("active");
    $('.select2').select2();
    $('#txtInstallDate').datepicker({
        format: 'yyyy-mm-dd',
        startDate:"2018-06-27",
        endDate: new Date(),
        autoclose: true
    });
    $('#txtInstallDate').datepicker('setDate', moment().subtract(2, 'days').format('YYYY-MM-DD'));

    $('#btnQuery').click(function() {
        queryData();
    });

    function queryData() {
        var date = moment($('#txtInstallDate').data('datepicker').dates[0]).format('YYYY-MM-DD');
        var filter = $('#filter').val();
        var filterCountry = $('#filterCountry').val();
        var xData = [];
        var yData = [];
        $("#canvas_dev").empty();
        $("#canvas_dev").append('<canvas id="canvas"></canvas>');
        $.post('query_app_ads_revenue_statistics', {
            date: date,
            appId: filter,
            countryCode: filterCountry
        }, function (result) {
            if (result && result.ret == 1) {
                xData = result.data_array;//X轴数据展示
                yData = result.date_array;//Y轴日期展示
                var dataSet = result.data_table;//表格数据
                setData(yData,xData);
                renderTable(dataSet);
            } else {
                alert(result.message);
            }
        }, 'json');
    }
    function setData(yData,xData) {
        var ctx = document.getElementById("canvas");
        var options = {
            animation:{
                duration:1000 ,//动画持续时长 ms
                easing:'easeOutQuart', // easing function to use
                onProgress: null, //动画过程中的回调函数
                onComplete:null  //动画结束后回调函数
            },
            layout: {   //全局设置
                padding: {    //图表边界距离
                    left: 50,
                    right: 0,
                    top: 0,
                    bottom: 0
                }
            },
            legend: {  //数据图例
                display: false,
                labels: {
                    fontColor: 'rgb(255, 99, 132)',
                    boxWidth:40,
                    fontSize:12,
                    fontStyle:"normal",
                    fontFamily:"Arial"
                },
                text:"legend name",
                hidden: false //设置显示或隐藏
            },
            title:{
                display:true,
                position:"top",
                fontSize:12,
                fontFamily:"Arial",
                fontColor:"#666",
                fontStyle:"bold",
                padding:10,
                lineHeight:1.2,
                text:"累计收入图表"
            },
            //工具条
            tooltip:{
                enabled:false,
                custom:null,
                mode:"nearest",
                intersect:true,
                position:"average",
                backgroundColor:"rgba(0,0,0,0.8)",
                titleFontFamily:"Arial",
                titleFontSize:12,
                titleFontStyle:"bold",
                titleFontColor:"#fff",
                titleSpacing:2,
                titleMarginBottom:6
            },
            //对点元素的设置
            elements:{
                radius:3,
                pointStyle:"circle",
                backgroundColor:"rgba(0,0,0,0.1)",
                borderWidth:1,
                borderColor:"rgba(0,0,0,0.1)",
                hitRadius:1,
                hoverRadius:4,
                hoverBorderWidth:1
            },
            scales: {
                xAxes: [{
                    type: 'linear',  // "linear" "category" "logarithmic" "time"
                    ticks: {
                        min:0,    //坐标轴的最小范围
                        // max:100   //坐标轴的最大范围
                    }
                }],
            }
        };
        var myChart = new Chart(ctx, {
            type: 'horizontalBar', // line:曲线 bar:垂直柱状 radar:雷达图 pie:饼图 doughnut：环图 polarArea:扇形图 scatter:散点 (默认的图类型)
            data: {
                labels : yData,
                datasets: [{   //数组格式：每个数组元素为一个数据系列
                    label: "累计收入",
                    data: xData, //数据系列的个数与labels参数里的元素个数相同
                    backgroundColor: '#96fcb7',  //也可以设为数组形式; 最后一个数为透明度(0-1)
                    borderColor:  'rgba(255,99,132,1)',
                    borderWidth: 1,  //设置边界宽度
                }
                ]
            },
            options:options
        });
    }

    function  renderTable(dataSet) {
        var columns = [
            { title: "安装日期" },
            { title: "花费" },
            { title: "总安装量" },
            { title: "购买安装量" },
            { title: "购买占比" },
            { title: "统计日期" },
            { title: "购买安装收入" },
            { title: "总安装累计收入" },
            { title: "购买安装累计收入" },
            { title: "购买累计收支比" }
        ];
        if ($.fn.DataTable.isDataTable("#metricTable")) {
            $('#metricTable').DataTable().clear().destroy();
        }

        $('#metricTable').DataTable({
            searching: false,
            paging: false,
            ordering: false,
            columns: columns,
            data:dataSet
        });
    }

</script>
</body>
</html>