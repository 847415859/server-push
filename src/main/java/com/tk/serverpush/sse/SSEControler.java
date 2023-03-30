package com.tk.serverpush.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
@CrossOrigin
public class SSEControler {
    //建立之后根据订单id，将SseEmitter存到ConcurrentHashMap
    //正常应该存到数据库里面，生成数据库订单，这里我们只是模拟一下
    public static final ConcurrentHashMap<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    //第2步：接受用户建立长连接，表示该用户已支付，已支付就可以生成订单(未确认状态)
    @GetMapping("/orderpay")
    public SseEmitter orderpay(Long payid) {
        log.info("建立长链接：{}",payid);
        //设置默认的超时时间60秒，超时之后服务端主动关闭连接。
        SseEmitter emitter = new SseEmitter(60 * 1000L);
        sseEmitters.put(payid,emitter);
        emitter.onTimeout(() -> sseEmitters.remove(payid));
        return emitter;
    }

    //第3步：接受支付系统的支付结果告知，表明用户支付成功
    @GetMapping("/payback")
    public void payback (Long payid, HttpServletResponse response){
        //把SSE连接取出来
        SseEmitter emitter = sseEmitters.get(payid);
        log.info("获取到的SseEmitter:{}",emitter);
        try {
            if(emitter != null) {
                //第4步：由服务端告知浏览器端：该用户支付成功了
                emitter.send("用户支付成功"); //触发前端message事件。
                //触发前端自定义的finish事件
                emitter.send(SseEmitter.event().name("finish").id("6666").data("哈哈"));
            }
        } catch (IOException e) {
            emitter.completeWithError(e);   //出发前端onerror事件
        }
    }
}