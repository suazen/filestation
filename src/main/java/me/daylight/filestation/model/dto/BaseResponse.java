package me.daylight.filestation.model.dto;

/**
 * @author Daylight
 * @date 2018/12/30 16:32
 */
public class BaseResponse {
    private int code;

    private String msg;

    private Object data;

    public BaseResponse() {
        this.code =200;
        this.msg="success";
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
