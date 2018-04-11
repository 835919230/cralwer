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
 * 往返程爬虫
 */
public class Wrapper_gjsairqr001 implements QunarCrawler<ProcessResultInfo>
{
    private QFHttpClient httpClient = null;
    private String cookie = null;

    // 往返航班
    public static void main(String[] args)
    {
        FlightSearchParam searchParam = new FlightSearchParam();

        searchParam.setDep("PVG");
        searchParam.setArr("EZE");
        // 出发日期
        searchParam.setDepDate("2018-4-10");
        // 返程日期
        searchParam.setRetDate("2018-06-10");
//        searchParam.setFlightNo("QR875/QR149_QR150/QR874");

        searchParam.setPassengerCount("1");
        searchParam.setTimeOut("60000");
        searchParam.setWrapperid("gjsairqr001");
        searchParam.setToken("");
        // 获取请求返回的html
        Wrapper_gjsairqr001 w = new Wrapper_gjsairqr001();
        String html = w.getHtml(searchParam);
        ProcessResultInfo result = new ProcessResultInfo();
        result = w.process(html, searchParam);
        System.out.println(result);
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

            // //////System.out.println(getUrl);
            String getUrl =
                    "https://booking.qatarairways.com/nsp/views/showBooking.action?widget=BF&selLang=en&tripType=R&fromStation="
                            + depCode
                            + "&toStation="
                            + arrCode
                            + "&departing="
                            + param.getDepDate()
                            + "&returning="
                            + param.getRetDate()
                            + "&bookingClass=E&adults=1&children=0&infants=0&searchType=F&addTaxToFare=Y&flexibleDate=off&minPurTime=null&upsellCallId=100&CID=AFALL151980";

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

    /*
     * 思路：
     * 去程fareFamily[]数组，fareFamily[i]表示这一行的三个不同价格
     * fareFamily[i] 中的relatedFareFamily[]数组，表示选定的这个价格 对应的返程价格
     * 其中返程根据 fareDisplayKey(inBdFltLst_1_ECRRNAR2)来确定唯一
     * 
     * priceMap<去程航班号+返程fareDisplayKey , 往返总价格>
     */
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

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String currency = StringUtils.substringBetween(html, "\"currencyCode\":\"", "\"");
            if (StringUtils.isEmpty(currency))
            {
                currency = "USD";
            }

            JSONObject obj = JSONObject.parseObject(html);

            // JSONArray colFareClassIn = obj.getJSONArray("colInboundLightFareClasses");
            // Map<String, String> qinfoMapOut = getQMap(colFareClass);
            // Map<String, String> qinfoMapIn = getQMap(colFareClassIn);

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

            JSONArray outboundRoutes = upsellResponse.getJSONArray("outBoundflights");
            JSONArray inboundRoutes = upsellResponse.getJSONArray("inBoundflights");
            if (outboundRoutes == null || outboundRoutes.size() == 0 || inboundRoutes == null
                    || inboundRoutes.size() == 0)
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

            List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>(); // 获得具体航班信息
            for (int out = 0; out < outboundRoutes.size(); out++)
            { // 去程
                JSONObject segmentsOut = outboundRoutes.getJSONObject(out);
                JSONObject flightItinerary = segmentsOut.getJSONObject("flightItinerary");
                JSONArray segmentArrayOut = flightItinerary.getJSONArray("flightVOs");
                if (segmentArrayOut == null || segmentArrayOut.size() == 0)
                {
                    continue;
                }

                List<FlightSegement> gosegs = new ArrayList<FlightSegement>(); // 去程
                List<String> flightNoList = new ArrayList<String>();

                for (int j = 0; j < segmentArrayOut.size(); j++)
                { // 去程
                    JSONObject legJson = segmentArrayOut.getJSONObject(j);
                    FlightSegement seg = new FlightSegement();

                    String flightNo = legJson.getString("flightNumber");
                    String company = legJson.getString("carrier");
                    flightNo = company + flightNo;

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
                    gosegs.add(seg);
                }

                JSONArray parray = segmentsOut.getJSONArray("fares");
                double pOut = 0d;
                double ptOut = 0d;
                Map<String, String> priceMap = new HashMap<String, String>();
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
                    // //////System.out.println("out* " + price);

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

                    pOut = sub(allprice, tx);
                    ptOut = tx;

                    // 返程价格
                    JSONArray pa = priceJson.getJSONArray("combinableFares");
                    for (int i = 0; i < pa.size(); i++)
                    {
                        JSONObject pobj = pa.getJSONObject(i);
                        JSONArray fInfo = pobj.getJSONArray("fareInfo");
                        JSONObject fligthItinerary = pobj.getJSONObject("fligthItinerary");
                        // 返程
                        String flightItineraryId = fligthItinerary.getString("flightItineraryId");
                        // 根据flightItineraryId匹配返程价格
                        for (int j = 0; j < fInfo.size(); j++)
                        {
                            JSONObject fobj = fInfo.getJSONObject(j);
                            // String fFamily = fobj.getString("fareFamily");
                            String boundAmount = fobj.getString("boundAmount");
                            String boundTaxAmount = fobj.getString("boundTaxAmount");
                            priceMap.put(flightItineraryId, boundAmount + "-" + boundTaxAmount);
                            break;
                        }
                    }
                    break;
                }

                for (int in = 0; in < inboundRoutes.size(); in++)
                { // 返程
                    FlightDetail flightDetail = new FlightDetail();

                    List<FlightSegement> resegs = new ArrayList<FlightSegement>(); // 返程
                    List<String> flightRetNoList = new ArrayList<String>();
                    // String cabinsIn = "";
                    String fNosIn = "";

                    JSONObject segmentsIn = inboundRoutes.getJSONObject(in);
                    JSONObject flightItineraryIn = segmentsIn.getJSONObject("flightItinerary");
                    String flightItineraryId = flightItineraryIn.getString("flightItineraryId");

                    JSONArray segmentArrayIn = flightItineraryIn.getJSONArray("flightVOs");
                    if (segmentArrayIn == null || segmentArrayIn.size() == 0)
                    {
                        continue;
                    }

                    for (int r = 0; r < segmentArrayIn.size(); r++)
                    { // 返程
                        JSONObject legJson = segmentArrayIn.getJSONObject(r);
                        // FlightSegement
                        FlightSegement seg = new FlightSegement();

                        String flightNo = legJson.getString("flightNumber");
                        String company = legJson.getString("carrier");
                        flightNo = company + flightNo;

                        fNosIn = fNosIn + flightNo;

                        String depIATACode = legJson.getString("depStation");
                        String arrIATACode = legJson.getString("arrStation");
                        String aircraft = legJson.getJSONObject("aircraft").getString("equipmentName");

                        String depTime = getFDate(legJson.getString("departureDate"));
                        String arrTime = getFDate(legJson.getString("arrivalDate"));
                        String[] ds = depTime.split(" ");
                        String[] as = arrTime.split(" ");

                        flightRetNoList.add(flightNo);
                        seg.setAircraft(aircraft);
                        seg.setCompany(company);
                        seg.setFlightno(flightNo);
                        seg.setDepDate(ds[0]);
                        seg.setArrDate(as[0]);
                        seg.setDepairport(depIATACode);
                        seg.setArrairport(arrIATACode);
                        seg.setDeptime(ds[1]);
                        seg.setArrtime(as[1]);
                        resegs.add(seg);
                    }

                    double pIn = 0d;
                    double ptIn = 0d;

                    String pInTemp = priceMap.get(flightItineraryId);
                    if (StringUtils.isEmpty(pInTemp))
                    {
                        JSONArray pretarray = segmentsIn.getJSONArray("fares");
                        for (int p = 0; p < pretarray.size(); p++)
                        {
                            JSONObject priceJson = pretarray.getJSONObject(p);
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
                            // //////System.out.println("out* " + price);

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

                            pIn = sub(allprice, tx);
                            ptIn = tx;
                            break;
                        }
                    }else
                    {
                        // boundAmount + "-" + boundTaxAmount
                        String[] pIns = pInTemp.split("-");

                        // String price = fareInfo.getString("boundAmount"); // 总票价
                        String price = pIns[0]; // 总票价
                        // //////System.out.println("in* " + price);
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

                        // String tax = fareInfo.getString("boundTaxAmount"); // 总票价
                        String tax = pIns[1]; // 总票价
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

                        pIn = sub(allprice, tx);
                        ptIn = tx;

                        if (pIn == 0d)
                        {
                            continue;
                        }
                    }

                    flightDetail.setDepdate(sdf.parse(arg1.getDepDate()));
                    flightDetail.setFlightno(flightNoList);

                    flightDetail.setMonetaryunit(currency);

                    flightDetail.setPrice(sum(pOut, pIn));
                    flightDetail.setTax(sum(ptOut, ptIn));
                    flightDetail.setDepcity(arg1.getDep());
                    flightDetail.setArrcity(arg1.getArr());
                    flightDetail.setWrapperid(arg1.getWrapperid());

                    RoundTripFlightInfo rtFlight = new RoundTripFlightInfo();
                    rtFlight.setOutboundPrice(pOut);
                    rtFlight.setReturnedPrice(pIn);
                    rtFlight.setRetflightno(flightRetNoList); // 返程航班号
                    rtFlight.setRetinfo(resegs); // 返程航班段
                    rtFlight.setDetail(flightDetail); // detail
                    rtFlight.setInfo(gosegs); // 去程航班段
                    rtFlight.setRetdepdate(sdf.parse(arg1.getRetDate())); // 返程日期

                    flightList.add(rtFlight);
                }
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

        } catch (Exception e)
        {// 解析失败
            //////e.printStackTrace();
            result.setRet(false);
            result.setStatus(Constants.PARSING_FAIL);
        }
        return result;
    }

    public double sum(double d1, double d2)
    {
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.add(bd2).doubleValue();
    }

    public String getFDate(String d) throws ParseException
    {
        Date date = new Date(Long.parseLong(d));// 2014-12-11
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3")); // GMT UTC
        return dateFormat.format(date);
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

    private Map<String, String> getQMap(JSONArray colFareClass)
    {
        Map<String, String> qinfoMap = new HashMap<String, String>();
        for (int i = 0; i < colFareClass.size(); i++)
        {
            JSONObject qobj = colFareClass.getJSONObject(i);
            String key = qobj.getString("id");
            JSONArray terms = qobj.getJSONArray("terms");
            String t = terms.toJSONString();
            String v = "";
            String[] ts = StringUtils.substringsBetween(t, "\"", "\"");
            for (String s : ts)
            {
                String k = StringUtils.substringBetween(s, "<b>", "<");
                String va = StringUtils.substringAfter(s, "&nbsp;").trim();
                String temp = k + ":" + va;
                v = v + " " + temp + ";";
            }
            qinfoMap.put(key, v.trim());
        }

        return qinfoMap;
    }

    private String formatDate(String date)
    {
        // 20/12/2015
        String d[] = date.split("/");
        return d[2] + "-" + d[1] + "-" + d[0];
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
                "https://booking.qatarairways.com/nsp/views/showBooking.action?widget=BF&selLang=en&tripType=R&fromStation="
                        + depCode
                        + "&toStation="
                        + arrCode
                        + "&departing="
                        + param.getDepDate()
                        + "&returning="
                        + param.getRetDate()
                        + "&bookingClass=E&adults=1&children=0&infants=0&searchType=F&addTaxToFare=Y&flexibleDate=off&minPurTime=null&upsellCallId=100&CID=AFALL151980";

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