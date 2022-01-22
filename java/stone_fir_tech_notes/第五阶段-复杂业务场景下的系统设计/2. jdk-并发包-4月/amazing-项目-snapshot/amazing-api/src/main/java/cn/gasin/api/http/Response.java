package cn.gasin.api.http;

import lombok.*;

/**
 * 注册响应
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    ResponseStatus status;
    String message;

    public static Response success(String message) {
        return Response.builder().status(ResponseStatus.SUCCESS).message(message).build();
    }

    public static Response failed(String message) {
        return Response.builder().status(ResponseStatus.FAILED).message(message).build();
    }
}
