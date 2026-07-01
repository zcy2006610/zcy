package com.zcy.forum.common;

import com.zcy.forum.constant.ResultConstant;
import lombok.Data;

@Data
public class Result<T> {
    private String msg;
    private Integer code;
    private T data;

    private Result(String msg,Integer code,T data){
        this.msg=msg;
        this.code=code;
        this.data=data;
    }

    public static<T>  Result<T> ok(){
        return new Result<>(ResultConstant.SUCCESS.getMsg(),ResultConstant.SUCCESS.getCode(),null);
    }

    public static<T> Result<T> ok(T data){
        return new Result<>(ResultConstant.SUCCESS.getMsg(),ResultConstant.SUCCESS.getCode(),data);
    }

    public static<T> Result<T> fail(){
        return new Result<>(ResultConstant.FAIL.getMsg(),ResultConstant.FAIL.getCode(),null);
    }

    public static<T> Result<T> fail(String msg,Integer code){
        return new Result<>(msg,code,null);
    }

}
