package com.tk.serverpush.sse.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
public class SseEmitterService {

    String str = "《ChatGPT AI 问答助手》 开源免费项目，涵盖爬虫接口、ChatGPT API对接、DDD架构设计、镜像打包、Docker容器部署，小巧精悍，流程全面。对于Java编程伙伴来说，非常具有学习价值。\n" +
            "\n" +
            "❤️ 这个项目本身是小傅哥为自己的知识星球开发的一个智能问答回复系统，用于帮助读者解决一些常见的技术问题，提高回答效率也减少小傅哥的对此类问题的时间投入。通过演示我们可以看到，有了这样一个智能AI问答助手，可以大大的减少很多对于这些通用类技术问题的回复，同时也可以把这样的问答内容沉淀到知识星球，方便其他人学习使用。\n" +
            "\n" +
            "《ChatGPT AI 问答助手》这样一个项目，要用到哪些技术手段呢？它包含；SpringBoot、DDD架构、Github仓库使用、接口爬虫、AI接口对接、定时任务、镜像打包、Docker容器部署等内容。\n" +
            "\n" +
            "可以说麻雀虽小，五脏俱全。代码量不大但流程很完整，对于正在学习Java的伙伴来说，非常具有学习价值。\n" +
            "\n" +
            "为了让粉丝伙伴更好的学习这个项目，小傅哥把它免费开源出来，并且是录制好对应的视频课程，一行行带着大家手写代码学习这个项目。\n" +
            "\n" +
            "包括工程的创建、Github仓库使用、push代码等，因为只有这样才能让更多新人有一条进入学习编程的大门。\n" +
            "\n";
	
    /**
     * 容器，保存连接，用于输出返回
     */
    private static Map<String, SseEmitter> sseCache = new ConcurrentHashMap<>();
    
    public SseEmitter createSseConnect(String clientId) {
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(0L);
        // 是否需要给客户端推送ID
        if (StringUtils.isEmpty(clientId)) {
            clientId = IdUtil.simpleUUID();
        }
        // 注册回调
        sseEmitter.onCompletion(completionCallBack(clientId));
        sseEmitter.onError(errorCallBack(clientId));
        sseEmitter.onTimeout(timeoutCallBack(clientId));
        sseCache.put(clientId, sseEmitter);
        log.info("创建新的sse连接，当前用户：{}", clientId);
        return sseEmitter;
    }

    public void closeSseConnect(String clientId) {
        SseEmitter sseEmitter = sseCache.get(clientId);
        if (sseEmitter != null) {
            sseEmitter.complete();
            removeUser(clientId);
        }
    }

	// 根据客户端id获取SseEmitter对象
    public SseEmitter getSseEmitterByClientId(String clientId) {
        return sseCache.get(clientId);
    }

    /**
     * 长链接完成后回调接口(即关闭连接时调用)
     *
     * @param clientId 客户端ID
     **/
    private Runnable completionCallBack(String clientId) {
        return () -> {
            log.info("结束连接：{}", clientId);
            removeUser(clientId);
        };
    }

    /**
     * 连接超时时调用
     *
     * @param clientId 客户端ID
     **/
    private Runnable timeoutCallBack(String clientId) {
        return () -> {
            log.info("连接超时：{}", clientId);
            removeUser(clientId);
        };
    }

    /**
     * 推送消息异常时，回调方法
     *
     * @param clientId 客户端ID
     * @return java.util.function.Consumer<java.lang.Throwable>
     **/
    private Consumer<Throwable> errorCallBack(String clientId) {
        return throwable -> {
            log.error("SseEmitterServiceImpl[errorCallBack]：连接异常,客户端ID:{}", clientId);

            // 推送消息失败后，每隔10s推送一次，推送5次
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(10000);
                    SseEmitter sseEmitter = sseCache.get(clientId);
                    if (sseEmitter == null) {
                        log.error("SseEmitterServiceImpl[errorCallBack]：第{}次消息重推失败,未获取到 {} 对应的长链接", i + 1, clientId);
                        continue;
                    }
                    sseEmitter.send("失败后重新推送");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 移除用户连接
     *
     * @param clientId 客户端ID
     **/
    private void removeUser(String clientId) {
        sseCache.remove(clientId);
        log.info("SseEmitterServiceImpl[removeUser]:移除用户：{}", clientId);
    }

    /**
     * 模仿ChatGPT回答问题
     * @param clientId  客户端id
     * @param question  问题
     */
    public void answer(String clientId, String question) {
        SseEmitter sseEmitter = sseCache.get(clientId);
        try {
            int readIndex = 0;
            int readEnd = 0;
            while (readEnd < str.length()) {
                int randomInt = RandomUtil.randomInt(5) + 1;
                readEnd = readIndex + randomInt;
                String sendMsg = str.substring(readIndex, Math.min(readEnd, str.length()));
                log.info("sendMsg :{}",sendMsg);
                // SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event().name("chatgpt").data(sendMsg).id(clientId);
                // sseEmitter.send(sseEventBuilder);
                sseEmitter.send(sendMsg);
                readIndex = readEnd;
                TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(500));
            }
        } catch (IOException e) {
            log.error("SseEmitterServiceImpl[createSseConnect]: 创建长链接异常，客户端ID:{}", clientId, e);
            throw new RuntimeException("创建连接异常！", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void responsePush(HttpServletResponse response) {
        try {
            int readIndex = 0;
            int readEnd = 0;
            response.setContentType("text/event-stream;charset=UTF-8");
            while (readEnd < str.length()) {
                int randomInt = RandomUtil.randomInt(5) + 1;
                readEnd = readIndex + randomInt;
                String sendMsg = "data:" + str.substring(readIndex, Math.min(readEnd, str.length())) + "\n\n";
                readIndex = readEnd;
                response.getWriter().print(sendMsg);
                response.getWriter().flush();
                TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(500));
            }
        } catch (IOException e) {
            log.error("SseEmitterServiceImpl[createSseConnect]: 创建长链接异常，客户端ID:{}", response, e);
            throw new RuntimeException("创建连接异常！", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
