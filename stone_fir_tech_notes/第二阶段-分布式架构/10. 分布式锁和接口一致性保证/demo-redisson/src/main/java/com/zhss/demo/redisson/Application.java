package com.zhss.demo.redisson;

import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

import java.util.Date;

public class Application {

	public static void main(String[] args) throws Exception {
		Config config = new Config();
		config.useClusterServers()
		    .addNodeAddress("redis://192.168.31.114:7001")
		    .addNodeAddress("redis://192.168.31.114:7002")
		    .addNodeAddress("redis://192.168.31.114:7003")
		    .addNodeAddress("redis://192.168.31.184:7001")
		    .addNodeAddress("redis://192.168.31.184:7002")
		    .addNodeAddress("redis://192.168.31.184:7003");

		final RedissonClient redisson = Redisson.create(config);

//		普通的可重入锁
//		RLock lock = redisson.getLock("anyLock");
//		lock.lock();
//		lock.unlock();
//
//		RMap<String, Object> map = redisson.getMap("anyMap");
//		map.put("foo", "bar");
//
//		map = redisson.getMap("anyMap");
//		System.out.println(map.get("foo"));

//		semaphore信号量: 总数限制
//		final RSemaphore semaphore = redisson.getSemaphore("semaphore");
//		// 设置信号量的上限允许通过并发量
//		semaphore.trySetPermits(3);
//		// 使用semaphore, acquire拿锁, 最多可以拿3次, 其他的之后等到release之后.
//		semaphore.acquire();
//		semaphore.release();



		// countdownLatch: 最低门槛限制

		RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");
		// 1. 设置门槛
		latch.trySetCount(3);

		// 开启三个线程
		for(int i = 0; i < 3; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						// 线程里面计数
						System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]在做一些操作，请耐心等待。。。。。。");
						Thread.sleep(3000);
						RCountDownLatch localLatch = redisson.getCountDownLatch("anyCountDownLatch");
						localLatch.countDown();
						System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]执行countDown操作");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}).start();
		}

		latch.await();
		System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]收到通知，有3个线程都执行了countDown操作，可以继续往下走");

	}
	
}
