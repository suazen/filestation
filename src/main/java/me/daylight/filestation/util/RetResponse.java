package me.daylight.filestation.util;

import me.daylight.filestation.model.dto.BaseResponse;

/**
 * @author Daylight
 * @date 2018/12/30 16:34
 */
public class RetResponse {
    private RetResponse(){}

    public static BaseResponse success(){
        return new BaseResponse();
    }

    public static BaseResponse success(Object object){
        BaseResponse response=new BaseResponse();
        response.setData(object);
        return response;
    }

    public static BaseResponse error(){
        BaseResponse response=new BaseResponse();
        response.setCode(400);
        response.setMsg("error");
        return response;
    }

    public static BaseResponse error(String msg){
        BaseResponse response=new BaseResponse();
        response.setCode(400);
        response.setMsg(msg);
        return response;
    }
}
