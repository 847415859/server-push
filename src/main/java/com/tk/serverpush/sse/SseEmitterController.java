package com.tk.serverpush.sse;

import com.tk.serverpush.sse.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;

/**
 * SSE长链接
 *
 * @author re
 * @date 2021/12/13
 */
@RestController
@RequestMapping("/sse")
@CrossOrigin
public class SseEmitterController {

    @Autowired
    private SseEmitterService sseEmitterService;

    /**
     * 创建SSE长链接
     *
     * @param clientId   客户端唯一ID(如果为空，则由后端生成并返回给前端)
     * @return org.springframework.web.servlet.mvc.method.annotation.SseEmitter
     * @author re
     * @date 2021/12/12
     **/
    @CrossOrigin //如果nginx做了跨域处理，此处可去掉
    @GetMapping("/createSseConnect")
    public SseEmitter createSseConnect(@RequestParam(name = "clientId", required = false) String clientId,HttpServletResponse response) {
        response.setContentType("text/event-stream;charset=UTF-8");
        return sseEmitterService.createSseConnect(clientId);
    }



    @CrossOrigin //如果nginx做了跨域处理，此处可去掉
    @GetMapping(value = "/question",produces = "text/event-stream;charset=UTF-8")
    public void createSseConnect(String clientId,String question) {
         sseEmitterService.answer(clientId,question);
    }


    @CrossOrigin //如果nginx做了跨域处理，此处可去掉
    @GetMapping("/responsePush")
    public void responsePush(HttpServletResponse response) {
        sseEmitterService.responsePush(response);
    }




    // /**
    //  * 关闭SSE连接
    //  *
    //  * @param clientId 客户端ID
    //  * @author re
    //  * @date 2021/12/13
    //  **/
    // @GetMapping("/CloseSseConnect")
    // public Result closeSseConnect(String clientId) {
    //     // sseEmitterService.closeSseConnect(clientId);
    //     // return ResultGenerator.genSuccessResult(true);
    // }

}
