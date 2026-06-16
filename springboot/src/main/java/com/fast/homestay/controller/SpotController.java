package com.fast.homestay.controller;

import com.fast.homestay.domain.Spot;
import com.fast.system.domain.TableDataInfo;
import com.fast.homestay.service.SpotService;
import com.fast.system.controller.BaseController;
import com.fast.system.domain.AjaxResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author LJH
 * @version 2.0
 */
@RestController
@RequestMapping("/homestay/spot")
public class SpotController extends BaseController {
    @Resource
    private SpotService spotService;


    //查询景点列表
    @GetMapping("/selectSpotList")
    public TableDataInfo selectSpotList(Spot spot){
        startPage();
        List<Spot> list = spotService.selectSpotList(spot);
        return getDataTable(list);
    }

    //根据景点id查询景点信息
    @GetMapping("/selectSpotBySpotId/{spotId}")
    public AjaxResult selectSpotBySpotId(@PathVariable String spotId){
        return success(spotService.selectSpotBySpotId(spotId));
    }

    //新增景点
    @PostMapping("/insertSpot")
    public AjaxResult insertSpot(@RequestBody Spot spot){
        return toAjax(spotService.insertSpot(spot));
    }

    //修改景点
    @PutMapping("/updateSpot")
    public AjaxResult updateSpot(@RequestBody Spot spot){
        return toAjax(spotService.updateSpot(spot));
    }

    //删除景点
    @DeleteMapping("/deleteSpotByIds/{spotIds}")
    public AjaxResult deleteSpotByIds(@PathVariable String[] spotIds){
        return toAjax(spotService.deleteSpotByIds(spotIds));
    }

}
