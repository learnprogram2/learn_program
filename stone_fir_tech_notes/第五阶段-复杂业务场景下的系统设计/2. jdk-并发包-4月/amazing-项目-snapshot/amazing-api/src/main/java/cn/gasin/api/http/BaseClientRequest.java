package cn.gasin.api.http;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseClientRequest {

    // TODO 这个字段后来挪动过, 应该有很多地方没有填充.
    @Setter
    RequestType requestType;


    // 服务坐标: name&id
    String serviceName;
    String instanceId; // 这个instanceID, 我觉得就把hostName和instanceID结合在一起了吧~ 还需要hostName干什么呢?

}
