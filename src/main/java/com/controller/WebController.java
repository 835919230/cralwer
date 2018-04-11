package com.controller;

import com.model.FlightInfo;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.service.SingleDstCrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by HeXi on 2018/4/5.
 */
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private SingleDstCrawlerService singleDstCrawlerService;

    @Autowired
    public WebController(SingleDstCrawlerService service) {
        this.singleDstCrawlerService = service;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexPage() {
        return "index";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String flightUserPage() {
        return "user";
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<FlightInfo>> search(@RequestParam("depCity") String dep,
                                             @RequestParam("arrCity") String arr,
                                             @RequestParam("depDate") String depDate,
                                             @RequestParam("passengerCount") String passengerCount) {
        FlightSearchParam searchParam = new FlightSearchParam();
        searchParam.setDep(dep);
        searchParam.setArr(arr);
        searchParam.setDepDate(depDate);
        searchParam.setPassengerCount(passengerCount);
        logger.info("search方法接收到请求参数：depCity:{}, arrCity:{}, depDate:{}, passengerCount:{}", dep, arr, depDate, passengerCount);
        searchParam.setTimeOut("60000");
        searchParam.setWrapperid("gjdairqr00b");
        return singleDstCrawlerService.search(searchParam);
    }

    @RequestMapping(value = "/book", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> book(@RequestParam("id") long id,
                                  @RequestParam(value = "number") String number) {
        FlightSearchParam param = new FlightSearchParam();
        param.setQueryId(String.valueOf(id));
        param.setPassengerCount(number);
        param.setWrapperid("gjdairqr00b");
        return singleDstCrawlerService.book(param);
    }

    @RequestMapping(value = "/order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> order(@RequestParam("id") long id,
                                   @RequestParam(value = "number") String number) {
        FlightSearchParam param = new FlightSearchParam();
        param.setQueryId(String.valueOf(id));
        param.setPassengerCount(number);
        param.setWrapperid("gjdairqr00b");
        return singleDstCrawlerService.book(param);
    }

    @RequestMapping(value = "/completeInformation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> completeInformation() {
        return null;
    }
}
