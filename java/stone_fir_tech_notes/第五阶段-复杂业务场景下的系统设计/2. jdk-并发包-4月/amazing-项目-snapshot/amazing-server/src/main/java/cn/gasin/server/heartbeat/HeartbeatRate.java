package cn.gasin.server.heartbeat;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳计数服务:
 */
@Component
public class HeartbeatRate {
    //    这个实现没有考虑一分钟内没有心跳得时候, 如果没有的话, count就没有意义了.
    //    private int lastMinuteCount;
    //    private long lastMinuteStartTimestamp;
    //
    //    public void count() {
    //        if (System.currentTimeMillis() - lastMinuteStartTimestamp > 60 * 1000) {
    //            lastMinuteStartTimestamp = System.currentTimeMillis();
    //            lastMinuteCount = 0;
    //        }
    //        lastMinuteCount++;
    //    }
    private long currentMinuteStartTimestamp = System.currentTimeMillis();
    private AtomicInteger currentMinuteCount = new AtomicInteger(0);
    private AtomicInteger lastMinuteCount = new AtomicInteger(0);

    public void count() {
        newMinute();
        currentMinuteCount.incrementAndGet();
    }

    /**
     * 拿到最近完整的一分钟内的计数: 距离当下可能是0-59s
     * 原来的synchronized锁, 其实性能不差, 而且就是一个心跳计数, 能有多少个实例? 1w个, 每分钟约2w请求, 并发就算500qps, 这个计数快到1ms就执行完了.
     */
    public int getLastMinuteCount() {
        newMinute();
        return lastMinuteCount.get();
    }

    private synchronized void newMinute() {
        if (System.currentTimeMillis() - currentMinuteStartTimestamp >= 60 * 1000) {
            // 这一段更新的, 有可能有并发问题:
            lastMinuteCount.set(currentMinuteCount.intValue());
            currentMinuteCount.set(0);
            currentMinuteStartTimestamp = System.currentTimeMillis();
        }
    }

}
