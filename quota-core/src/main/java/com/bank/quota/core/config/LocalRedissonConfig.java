package com.bank.quota.core.config;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本地运行 profile 下的 RedissonClient 配置。
 *
 * <p>本地无 Redis 时，{@code Redisson.create} 在 3.25.0 会立即连接导致启动失败。
 * 这里用动态代理返回一个 stub：bean 实例化不触发连接，{@code getLock} 返回一个
 * 本地放行的 RLock stub（tryLock 恒为 true、unlock 空操作），使应用能在无 Redis
 * 环境下启动。仅用于本地验证，分布式锁语义不保证。</p>
 */
@Configuration
@Profile("local")
public class LocalRedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "getLock":
                case "getFairLock":
                    return lockStub();
                case "shutdown":
                case "shutdownNow":
                    return null;
                case "isShutdown":
                case "isShuttingDown":
                case "isInCluster":
                    return false;
                default:
                    return defaultReturn(method.getReturnType());
            }
        };
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class},
                handler);
    }

    private Object lockStub() {
        InvocationHandler lockHandler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "tryLock":
                    return true;
                case "isHeldByCurrentThread":
                    return true;
                case "isLocked":
                    return false;
                case "unlock":
                case "forceUnlock":
                case "lock":
                case "lockInterruptibly":
                case "close":
                    return null;
                case "getName":
                    return "local-lock";
                default:
                    return defaultReturn(method.getReturnType());
            }
        };
        return Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class<?>[]{RLock.class},
                lockHandler);
    }

    private static Object defaultReturn(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == double.class) return 0.0d;
        if (type == float.class) return 0.0f;
        return null;
    }

    /**
     * 本地 RedisTemplate stub：基于 {@link ConcurrentHashMap} 后端，支持
     * {@code opsForValue().set/get}（含 TTL）与 {@code delete}。
     * 仅用于本地验证，不保证分布式语义与持久化。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return new LocalRedisTemplate();
    }

    private static final class Entry {
        final Object value;
        final Long expireAt;
        Entry(Object value, Long expireAt) { this.value = value; this.expireAt = expireAt; }
    }

    private static final class LocalRedisTemplate extends RedisTemplate<String, Object> {
        private final Map<String, Entry> store = new ConcurrentHashMap<>();
        private final ValueOperations<String, Object> valueOps = buildValueOps();

        @Override
        public void afterPropertiesSet() {
            // 跳过父类对 RedisConnectionFactory / defaultSerializer 的校验
        }

        @Override
        public ValueOperations<String, Object> opsForValue() {
            return valueOps;
        }

        @Override
        public Boolean delete(String key) {
            return store.remove(key) != null;
        }

        @Override
        public Long delete(Collection<String> keys) {
            long count = 0;
            if (keys == null) return 0L;
            for (String k : keys) if (store.remove(k) != null) count++;
            return count;
        }

        @Override
        public Boolean hasKey(String key) {
            Entry e = store.get(key);
            if (e == null) return false;
            if (e.expireAt != null && System.currentTimeMillis() > e.expireAt) {
                store.remove(key);
                return false;
            }
            return true;
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            Entry e = store.get(key);
            if (e == null) return false;
            store.put(key, new Entry(e.value, System.currentTimeMillis() + unit.toMillis(timeout)));
            return true;
        }

        private Object get(String key) {
            Entry e = store.get(key);
            if (e == null) return null;
            if (e.expireAt != null && System.currentTimeMillis() > e.expireAt) {
                store.remove(key);
                return null;
            }
            return e.value;
        }

        /**
         * 用动态代理构建 ValueOperations，仅实现 set/get/delete/hasKey 等关键方法，
         * 其余方法返回类型默认值，避免实现庞大接口。
         */
        @SuppressWarnings("unchecked")
        private ValueOperations<String, Object> buildValueOps() {
            InvocationHandler h = (proxy, method, args) -> {
                String name = method.getName();
                switch (name) {
                    case "set":
                        if (args.length == 2) {
                            store.put((String) args[0], new Entry(args[1], null));
                            return null;
                        }
                        if (args.length == 4) {
                            long timeout = ((Number) args[2]).longValue();
                            TimeUnit unit = (TimeUnit) args[3];
                            store.put((String) args[0], new Entry(args[1], System.currentTimeMillis() + unit.toMillis(timeout)));
                            return null;
                        }
                        return null;
                    case "get":
                        return get((String) args[0]);
                    case "getAndSet":
                        Entry prev = store.put((String) args[0], new Entry(args[1], null));
                        return prev == null ? null : prev.value;
                    case "getAndDelete":
                        Entry rm = store.remove((String) args[0]);
                        return rm == null ? null : rm.value;
                    case "setIfAbsent":
                    case "setIfPresent":
                        return Boolean.FALSE;
                    case "delete":
                        return store.remove((String) args[0]) != null;
                    case "hasKey":
                        return hasKey((String) args[0]);
                    case "getOperations":
                        return this;
                    default:
                        return defaultReturn(method.getReturnType());
                }
            };
            return (ValueOperations<String, Object>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(),
                    new Class<?>[]{ValueOperations.class},
                    h);
        }
    }
}
