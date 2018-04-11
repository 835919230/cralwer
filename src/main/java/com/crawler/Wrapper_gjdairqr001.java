package com.crawler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 单程票爬虫
 */
public class Wrapper_gjdairqr001 implements QunarCrawler<ProcessResultInfo>
{
    private QFHttpClient httpClient = null;
    private String cookie = null;

    // 单程航班
    public static void main(String[] args)
    {
        FlightSearchParam searchParam = new FlightSearchParam();

        searchParam.setDep("EZE");
        searchParam.setArr("PVG");
        searchParam.setDepDate("2018-4-19");


        searchParam.setPassengerCount("1");
        searchParam.setTimeOut("60000");
        searchParam.setWrapperid("gjdairqr00b");
        // 获取请求返回的html
        Wrapper_gjdairqr001 w = new Wrapper_gjdairqr001();

        String html = w.getHtml(searchParam);
        ProcessResultInfo result = new ProcessResultInfo();
        result = w.process(html, searchParam);
        System.out.println(result);
        List<? extends BaseFlightInfo> data = result.getData();
        for (BaseFlightInfo info : data) {
            System.out.println(info);
        }
    }

    @Override
    public String getHtml(FlightSearchParam param)
    {
        QFGetMethod get = null;
        QFPostMethod post = null;
        try
        {
            // TODO 提交代码 注释掉！！！
//            Protocol myhttps = new Protocol("https", new MySSLProtocolSocketFactory(), 443);
//            Protocol.registerProtocol("https", myhttps);

            httpClient = new QFHttpClient(param, false);
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            String count = param.getPassengerCount();
            if (StringUtils.isEmpty(count))
            {
                param.setPassengerCount("1");
                count = "1";
            }
            if (Integer.parseInt(count) > 9)
            {
                return "PARAM_ERROR";
            }

            String depCode = param.getDep();
            String arrCode = param.getArr();
            if (StringUtils.isEmpty(depCode) || StringUtils.isEmpty(arrCode))
            {
                return "INVALID_AIRLINE";
            }

            // *** 3
            String getUrl =
                    "https://booking.qatarairways.com/nsp/views/showBooking.action?widget=BF&selLang=en&tripType=O&fromStation="
                            + depCode
                            + "&toStation="
                            + arrCode
                            + "&departing="
                            + param.getDepDate()
                            + "&returning=&bookingClass=E&adults="
                            + count
                            + "&children=0&infants=0&searchType=F&addTaxToFare=Y&flexibleDate=off&minPurTime=null&upsellCallId=100&CID=AFALL151980";

            // //////System.out.println(getUrl);

            get = new QFGetMethod(getUrl);
            String urlGet = ""; // get请求的url

            get.setFollowRedirects(false);
            get.getParams().setContentCharset("utf-8");
            // get.setRequestHeader("Referer", "http://www.qatarairways.com/cn/cn/homepage.page");
            get.setRequestHeader("Host", "booking.qatarairways.com");
            cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
            // //////System.out.println(cookie);
            // cookie = c + "; " + cookie;
            get.setRequestHeader("Cookie", "campaigntracking=AFALL151980");
            int status = httpClient.executeMethod(get);

            String html = "";
            if (get.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY
                    || get.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY)
            {

                Header location = get.getResponseHeader("Location");
                if (location != null)
                {
                    urlGet = location.getValue();
                    if (!urlGet.startsWith("http"))
                    {
                        urlGet = get.getURI().getScheme() + "://" + get.getURI().getHost()
                                + (get.getURI().getPort() == -1 ? "" : ":" + get.getURI().getPort()) + urlGet;
                    }
                } else
                {
                    return "";
                }
                // //////System.out.println(urlGet);

                get = new QFGetMethod(urlGet);
                get.getParams().setContentCharset("utf-8");
                cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
                // cookie = c + "; " + cookie;
                get.setRequestHeader("Cookie", cookie);
                status = httpClient.executeMethod(get);
                html = get.getResponseBodyAsString();
            }

            String vs = StringUtils.substringBetween(html, "<input type=\"hidden\" name=\"javax.faces.ViewState\"",
                    "/>");
            vs = StringUtils.substringBetween(vs, "value=\"", "\"");
            // //////System.out.println(vs);
            String searchToken = StringUtils.substringBetween(html,
                    "<input type=\"hidden\" name=\"searchToken\" value=\"", "\"");

            post = new QFPostMethod("https://booking.qatarairways.com/nsp/views/searchLoading.xhtml");
            List<NameValuePair> postData = new ArrayList<NameValuePair>();
            postData.add(new NameValuePair("searchToken", searchToken));
            postData.add(new NameValuePair("hidden_SUBMIT", "1"));
            postData.add(new NameValuePair("javax.faces.ViewState", vs));
            postData.add(new NameValuePair("javax.faces.behavior.event", "click"));
            postData.add(new NameValuePair("javax.faces.source", "hidden:searchLink"));
            postData.add(new NameValuePair("javax.faces.partial.ajax", "true"));
            postData.add(new NameValuePair("javax.faces.partial.execute", "hidden:searchLink"));
            postData.add(new NameValuePair("hidden", "hidden"));

            post.setRequestBody(postData.toArray(new NameValuePair[0]));
            post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            post.addRequestHeader("Faces-Request", "partial/ajax");
            post.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            post.addRequestHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
            post.addRequestHeader("Accept-Encoding", "gzip, deflate ");
            post.setRequestHeader("Host", "booking.qatarairways.com");
            post.setRequestHeader("Referer",
                    "https://booking.qatarairways.com/nsp/views/searchLoading.xhtml?selLang=en");
            cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
            // cookie = c + "; " + cookie;
            post.addRequestHeader("Cookie", cookie);

            status = httpClient.executeMethod(post);
            if (status != HttpStatus.SC_OK)
            {
                return "Exception";
            }
            // //////System.out.println(post.getResponseBodyAsString());

            getUrl = "https://booking.qatarairways.com/nsp/views/index.xhtml?cid=AFALL151980&faces-redirect=true.xhtml";
            get = new QFGetMethod(getUrl);
            get.getParams().setContentCharset("utf-8");
            get.setRequestHeader("Host", "booking.qatarairways.com");
            get.setRequestHeader("Referer",
                    "https://booking.qatarairways.com/nsp/views/searchLoading.xhtml?selLang=en");
            cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
            // cookie = c + "; " + cookie;
            // //////System.out.println(cookie);
            get.setRequestHeader("Cookie", cookie);
            status = httpClient.executeMethod(get);
            if (status != HttpStatus.SC_OK)
            {
                return "Exception";
            }
            html = get.getResponseBodyAsString();
            // //////System.out.println(html);

            post = new QFPostMethod("https://booking.qatarairways.com/nsp/flightServlet");
            post.getParams().setContentCharset("utf-8");
            post.addRequestHeader("Content-Type", "application/json");
            post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
            post.setRequestHeader("Host", "booking.qatarairways.com");
            post.setRequestHeader("Referer", "https://booking.qatarairways.com/nsp/views/index.xhtml");
            cookie = StringUtils.join(httpClient.getState().getCookies(), "; ");
            // cookie = c + "; " + cookie;
            post.addRequestHeader("Cookie", cookie);

            status = httpClient.executeMethod(post);
            if (status != HttpStatus.SC_OK)
            {
                return "Exception";
            }
            // //////System.out.println();

            return post.getResponseBodyAsString();
        } catch (Exception e)
        {
            if (e.toString().contains("403"))
            {
                return "Forbidden";
            }
            //////e.printStackTrace();
        } finally
        {
            if (null != post)
            {
                post.releaseConnection();
            }
            if (null != get)
            {
                get.releaseConnection();
            }
        }
        return "Exception";
    }

    @Override
    public ProcessResultInfo process(String html, FlightSearchParam arg1)
    {
        // //////System.out.println(html);
        ProcessResultInfo result = new ProcessResultInfo();
        if ("Exception".equals(html))
        {
            result.setRet(false);
            result.setStatus(Constants.CONNECTION_FAIL);
            return result;
        }

        if ("Forbidden".equals(html))
        {
            result.setRet(false);
            result.setStatus(Constants.IP_FORBIDDEN);
            return result;
        }
        if ("PARAM_ERROR".equals(html))
        {
            result.setRet(false);
            result.setStatus(Constants.PARAM_ERROR);
            return result;
        }
        if ("INVALID_DATE".equals(html))
        {
            result.setRet(false);
            result.setStatus(Constants.INVALID_DATE);
            return result;
        }

        String currency = StringUtils.substringBetween(html, "\"currencyCode\":\"", "\"");
        if (StringUtils.isEmpty(currency))
        {
            currency = "USD";
        }

        try
        {
            JSONObject obj = JSONObject.parseObject(html);

            JSONObject upsellResponse = obj.getJSONObject("upsellResponse");
            if (upsellResponse == null)
            {
                if (!StringUtils.isEmpty(arg1.getPassengerCount()) && Integer.parseInt(arg1.getPassengerCount()) > 1)
                {
                    result.setRet(false);
                    result.setStatus(Constants.SOLD_OUT);
                    return result;
                }
                if (!StringUtils.isEmpty(arg1.getFlightNo()))
                {
                    result.setRet(false);
                    result.setStatus(Constants.OTHER);
                    return result;
                }
                result.setRet(false);
                result.setStatus(Constants.INVALID_DATE);
                return result;
            }

            JSONArray ajson = upsellResponse.getJSONArray("outBoundflights");
            if (ajson == null || ajson.size() == 0)
            {
                if (!StringUtils.isEmpty(arg1.getPassengerCount()) && Integer.parseInt(arg1.getPassengerCount()) > 1)
                {
                    result.setRet(false);
                    result.setStatus(Constants.SOLD_OUT);
                    return result;
                }
                if (!StringUtils.isEmpty(arg1.getFlightNo()))
                {
                    result.setRet(false);
                    result.setStatus(Constants.OTHER);
                    return result;
                }
                result.setRet(false);
                result.setStatus(Constants.INVALID_DATE);
                return result;
            }

            List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
            for (int i = 0; i < ajson.size(); i++)
            {
                JSONObject ojson = ajson.getJSONObject(i);

                OneWayFlightInfo oneWayFlightInfo = new OneWayFlightInfo();
                List<FlightSegement> segs = new ArrayList<FlightSegement>();

                // flightDetail
                FlightDetail flightDetail = new FlightDetail();

                JSONObject flightItinerary = ojson.getJSONObject("flightItinerary");
                JSONArray legsJson = flightItinerary.getJSONArray("flightVOs");

                List<String> flightNoList = new ArrayList<String>();
                if (legsJson != null && legsJson.size() > 0)
                {
                    for (int j = 0; j < legsJson.size(); j++)
                    {
                        JSONObject legJson = legsJson.getJSONObject(j);
                        // FlightSegement
                        FlightSegement seg = new FlightSegement();

                        String flightNo = legJson.getString("flightNumber");
                        String company = legJson.getString("carrier");
                        flightNo = company + flightNo;
                        // QR871
                        // String nos = StringUtils.substring(flightNo, 2);
                        // while (nos.startsWith("0")) {
                        // nos = nos.substring(1);
                        // }
                        // flightNo = company + nos;

                        String depIATACode = legJson.getString("depStation");
                        String arrIATACode = legJson.getString("arrStation");
                        String aircraft = legJson.getJSONObject("aircraft").getString("equipmentName");

                        String depTime = getFDate(legJson.getString("departureDate"));
                        String arrTime = getFDate(legJson.getString("arrivalDate"));
                        String[] ds = depTime.split(" ");
                        String[] as = arrTime.split(" ");

                        flightNoList.add(flightNo);
                        seg.setAircraft(aircraft);
                        seg.setCompany(company);
                        seg.setFlightno(flightNo);
                        seg.setDepDate(ds[0]);
                        seg.setArrDate(as[0]);
                        seg.setDepairport(depIATACode);
                        seg.setArrairport(arrIATACode);
                        seg.setDeptime(ds[1]);
                        seg.setArrtime(as[1]);
                        segs.add(seg);
                    }
                } else
                {
                    continue;
                }

                JSONArray parray = ojson.getJSONArray("fares");
                for (int p = 0; p < parray.size(); p++)
                {
                    JSONObject priceJson = parray.getJSONObject(p);
                    if (priceJson == null)
                    {
                        continue;
                    }
                    JSONObject fareInfo = priceJson.getJSONObject("fareInfo");
                    String price = fareInfo.getString("boundAmount"); // 总票价
                    if (StringUtils.isEmpty(price))
                    {
                        continue;
                    }
                    double allprice = 0d;
                    if (!arg1.getPassengerCount().equals("1"))
                    {
                        allprice = division(price, arg1.getPassengerCount());
                    } else
                    {
                        allprice = Double.parseDouble(price);
                    }

                    String tax = fareInfo.getString("boundTaxAmount"); // 总票价
                    if (StringUtils.isEmpty(tax))
                    {
                        continue;
                    }
                    double tx = 0d;
                    if (!arg1.getPassengerCount().equals("1"))
                    {
                        tx = division(tax, arg1.getPassengerCount());
                    } else
                    {
                        tx = Double.parseDouble(tax);
                    }

                    flightDetail.setPrice(sub(allprice, tx));
                    flightDetail.setTax(tx);
                    break;
                }

                flightDetail.setDepdate(getDate(arg1.getDepDate()));
                flightDetail.setFlightno(flightNoList);
                flightDetail.setMonetaryunit(currency);
                flightDetail.setDepcity(arg1.getDep());
                flightDetail.setArrcity(arg1.getArr());
                flightDetail.setWrapperid(arg1.getWrapperid());

                oneWayFlightInfo.setDetail(flightDetail);
                oneWayFlightInfo.setInfo(segs);
                flightList.add(oneWayFlightInfo);
            }

            if (flightList.size() == 0)
            {
                if (!StringUtils.isEmpty(arg1.getPassengerCount()) && Integer.parseInt(arg1.getPassengerCount()) > 1)
                {
                    result.setRet(false);
                    result.setStatus(Constants.SOLD_OUT);
                    return result;
                }
                if (!StringUtils.isEmpty(arg1.getFlightNo()))
                {
                    result.setRet(false);
                    result.setStatus(Constants.OTHER);
                    return result;
                }
                result.setRet(false);
                result.setStatus(Constants.INVALID_DATE);
                return result;
            }

            result.setRet(true);
            result.setStatus(Constants.SUCCESS);
            result.setData(flightList);
            return result;
        } catch (Exception e)
        {
            //////e.printStackTrace();
            result.setRet(false);
            result.setStatus(Constants.PARSING_FAIL);
            return result;
        }
    }

    public Double sub(double v1, double v2)
    {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    private Double division(String p1, String count)
    {
        BigDecimal b1 = new BigDecimal(p1);
        BigDecimal b2 = new BigDecimal(count);
        BigDecimal b3 = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
        return b3.doubleValue();
    }

    /**
     * @param date :Sat Dec 20 07:25:00 GMT 2014
     * @return 2014-12-20 07:25:00
     */
    public String getFormatDate(String date)
    {
        if (date == null || date.length() == 0)
        {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.US);
        Date d;
        String dateStrTmp = "";
        try
        {
            d = sdf.parse(date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            dateStrTmp = dateFormat.format(d);
            // //////System.out.println(dateStrTmp);
        } catch (ParseException e)
        {
            //////e.printStackTrace();
        }
        return dateStrTmp.split(" ")[0];
    }

    /**
     * 返回解析后的date对象
     *
     * @param date --日期字符串 yyyy-MM-dd
     */
    public Date getDate(String date) throws ParseException
    {
        return getDateByFormat(date, "yyyy-MM-dd");
    }

    public String getFDate(String d) throws ParseException
    {
        Date date = new Date(Long.parseLong(d));// 2014-12-11
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3")); // GMT UTC
        return dateFormat.format(date);
    }

    /**
     * 返回解析后的date对象
     *
     * @param format -日期字符串的格式
     */
    public Date getDateByFormat(String date, String formatStr) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return StringUtils.isBlank(date) ? null : format.parse(date);
    }

    @Override
    public BookingResult getBookingInfo(FlightSearchParam param)
    {
        String count = param.getPassengerCount();
        if (StringUtils.isEmpty(count))
        {
            param.setPassengerCount("1");
            count = "1";
        }

        String depCode = param.getDep();
        String arrCode = param.getArr();

        String bookingUrlPre =
                "https://booking.qatarairways.com/nsp/views/showBooking.action?widget=BF&selLang=en&tripType=O&fromStation="
                        + depCode
                        + "&toStation="
                        + arrCode
                        + "&departing="
                        + param.getDepDate()
                        + "&returning=&bookingClass=E&adults="
                        + count
                        + "&children=0&infants=0&searchType=F&addTaxToFare=Y&flexibleDate=off&minPurTime=null&upsellCallId=100&CID=AFALL151980";

        BookingResult bookingResult = new BookingResult();

        BookingInfo bookingInfo = new BookingInfo();
        bookingInfo.setAction(bookingUrlPre);
        bookingInfo.setMethod("get");
        bookingResult.setData(bookingInfo);
        bookingResult.setRet(true);
        return bookingResult;
    }

    private String month2No(String month)
    {
        month = month.toLowerCase();
        if (month.equals("01"))
        {
            return "Jan";
        } else if (month.equals("02"))
        {
            return "Feb";
        } else if (month.equals("03"))
        {
            return "Mar";
        } else if (month.equals("04"))
        {
            return "Apr";
        } else if (month.equals("05"))
        {
            return "May";
        } else if (month.equals("06"))
        {
            return "Jun";
        } else if (month.equals("07"))
        {
            return "Jul";
        } else if (month.equals("08"))
        {
            return "Aug";
        } else if (month.equals("09"))
        {
            return "Sep";
        } else if (month.equals("10"))
        {
            return "Oct";
        } else if (month.equals("11"))
        {
            return "Nov";
        } else if (month.equals("12"))
        {
            return "Dec";
        }
        return null;
    }

}