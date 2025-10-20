package org.example.backend.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiResponse<T> {

    private String resultCode;
    private String msg;
    private T data;

    public ApiResponse(String resultCode, String msg) {
        this.resultCode = resultCode;
        this.msg = msg;
        this.data = null;
    }

    @JsonIgnore
    public int getStatusCode() {
        String statusCode = resultCode.split("-")[0];
        return Integer.parseInt(statusCode);
    }

}