<!DOCTYPE html>
<html>
<head>
    <%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
    <meta charset="UTF-8">
    <title>机票爬虫演示</title>
    <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- 可选的 Bootstrap 主题文件（一般不用引入） -->
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
          integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

    <script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.min.js" type="text/javascript"></script>

    <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"
            integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>

    <script >
        function book(id, number) {
            $.ajax({
                type: "POST",
                url: "/book",
                data: {
                    "id":id,
                    "number": number
                },
                success:function(result) {
                    if (result.success) {
                        window.location.href="/user"
                    } else {
                        alert(result.msg)
                    }
                },
                error:function () {
                    alert("系统异常")
                }
            });
        }

        function order(id, number) {
            $.ajax({
                type: "POST",
                url: "/order",
                data: {
                    "id":id,
                    "number": number
                },
                success:function(result) {
                    if (result.success) {
                        window.location.href="/user"
                    } else {
                        alert(result.msg)
                    }
                },
                error:function () {
                    alert("系统异常")
                }
            });
        }
    </script>
</head>
<body>
<div class="container1">
    <div class="row">
        <nav class="navbar navbar-default">
            <div class="container">
                <div class="navbar-header">
                    <a class="navbar-brand" href="javascript:void(0)">机票爬虫演示</a>
                </div>
                <div class="collapse navbar-collapse">
                    <form class="navbar-form navbar-left" role="search" method="post" action="">
                        <div class="form-group">
                            <input type="number" id="year" value="2018" min="2018" max="9999" class="form-control" placeholder="年">
                        </div>
                        <div class="form-group">
                            <input type="number" id="month" value="12" min="1" max="12" class="form-control" placeholder="月">
                        </div>
                        <div class="form-group">
                            <input type="number" id="day" value="19" min="1" max="31" class="form-control" placeholder="日">
                        </div>
                        <div class="form-group">
                            <input type="text" id="depCity" value="EZE" class="form-control" placeholder="起点">
                        </div>
                        <div class="form-group">
                            <input type="text" id="arrCity" value="PVG" class="form-control" placeholder="终点">
                        </div>
                        <div class="form-group">
                            <input type="number" id="passengerCount" value="1" min="0" class="form-control" placeholder="人数">
                        </div>
                        <button type="button" id="searchBtn" class="btn btn-default">搜索</button>
                    </form>
                </div>
            </div>
        </nav>
    </div>
    <div class="row">
        <div class="container">
            <table class="table">
                <caption>基本的表格布局</caption>
                <thead>
                <tr>
                    <th>出发地</th>
                    <th>目的地</th>
                    <th>出发日期</th>
                    <th>航班号</th>
                    <th>出发时间</th>
                    <th>价格</th>
                    <th>剩余座位</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody id="appendBody">
                <%--<tr>--%>
                    <%--<td>Tanmay</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>Bangalore</td>--%>
                    <%--<td>--%>
                        <%--<button type="button" id="bookBtn" class="btn btn-default">预定</button>--%>
                        <%--<button type="button" id="orderBtn" class="btn btn-default">下单</button>--%>
                    <%--</td>--%>
                <%--</tr>--%>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script>
    $('#searchBtn').click(function (e) {
        var number = $('#passengerCount').val();
        $.ajax({
            type: "POST",
            url: "/search",
            data: {
                "depCity": $('#depCity').val(),
                "arrCity": $('#arrCity').val(),
                "depDate": $('#year').val()+'-' + $('#month').val()+'-' + $('#day').val(),
                "passengerCount": number,
            },
            success:function(result) {
                console.log(result);
                if (!result.success) {
                    alert(result.msg);
                }
                var html = '';
                for(var i in result.data) {
                    var d = result.data[i];
                    html += '<tr>'+
                            '<td>'+d.depCity+'</td>'+
                            '<td>'+d.arrCity+'</td>'+
                            '<td>'+d.depDate+'</td>'+
                            '<td>'+d.flightNo+'</td>'+
                            '<td>'+d.depTime+'</td>'+
                            '<td>'+d.price+'</td>'+
                            '<td>'+d.seat+'</td>'+
                            '<td><button class="btn btn-default" onclick="book('+d.id+','+number+')">预定</button></td>'+
                            '<td><button class="btn btn-default" onclick="order('+d.id+','+number+')">下单</button></td>'+
                            '</tr>';
                }
                $('#appendBody').html(html);
            },
            error:function () {
                alert("系统异常")
            }
        });
    })
</script>
</body>
</html>