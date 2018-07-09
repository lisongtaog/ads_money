<%--
  Created by IntelliJ IDEA.
  User: mengjun
  Date: 2018/7/7
  Time: 17:15
  Desc: 包含所有链接的侧边栏
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

            <%-- id为0-3  start--%>
            <li role="menu_li">
                <a href="index.jsp">
                    <i class="fa fa-dashboard"></i> <span>Dashboard</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="app_management.jsp">
                    <i class="fa fa-files-o"></i>
                    <span>App Management</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="firebase_management.jsp">
                    <i class="fa fa-book"></i>
                    <span>Firebase Management</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="admob_account_management.jsp">
                    <i class="fa fa-th"></i>
                    <span>AdMob Account Management</span>
                </a>
            </li>
            <%-- id为0-3  end--%>

            <%-- id为4-7  start--%>

            <li role="menu_li">
                <a href="ad_unit_management.jsp">
                    <i class="fa fa-list-alt"></i>
                    <span>Ad Unit Management</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="user_defined_sql.jsp">
                    <i class="fa fa-scribd"></i>
                    <span>User Defined SQL</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="app_recom_report.jsp">
                    <i class="fa fa-folder"></i>
                    <span>App Recommend Report</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="app_trend.jsp">
                    <i class="fa fa-superpowers"></i>
                    <span>App Trend</span>
                </a>
            </li>
            <%-- id为4-7  end--%>

            <%-- id为8-12  start--%>

            <li role="menu_li">
                <a href="app_report.jsp">
                    <i class="fa fa-folder"></i>
                    <span>App Report</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="country_report.jsp">
                    <i class="fa fa-free-code-camp"></i>
                    <span>Country Report</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="ctr_monitor.jsp">
                    <i class="fa fa-check"></i>
                    <span>CTR Monitor</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="ad_impression_monitor.jsp">
                    <i class="fa fa-snowflake-o"></i>
                    <span>Ad Impression Monitor</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="active_user_ad_chance_report.jsp">
                    <i class="fa fa-microchip"></i>
                    <span>Active User Ad Chance</span>
                </a>
            </li>
            <%-- id为8-12  end--%>

            <%-- id为13-15  start--%>

            <li role="menu_li">
                <a href="app_hourly_trend.jsp">
                    <i class="fa fa-quora"></i>
                    <span>App Hourly Trend</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="app_ads_impressions_statistics.jsp">
                    <i class="fa fa-list-alt"></i>
                    <span>App Ads Impressions Statistics</span>
                </a>
            </li>
            <li role="menu_li">
                <a href="app_active_user_statistics.jsp">
                    <i class="fa fa-folder"></i>
                    <span>App Active User Statistics</span>
                </a>
            </li>
            <%-- id为13-15  end--%>

        </ul>
    </section>
    <!-- /.sidebar -->
</aside>
