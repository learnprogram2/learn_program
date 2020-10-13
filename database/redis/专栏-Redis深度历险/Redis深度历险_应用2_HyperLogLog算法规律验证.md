```java
/**
 * 给定一系列的随机整数，我们记录下低位连续零位的最大长度 k，
 * 通过这个 k 值可以估算出随机数的数量
 */
public class PfTest {
    static class BitKeeper {
        private int maxbits;

        public void random(long value) {
            int bits = lowZeros(value);
            if (bits > this.maxbits) {
                this.maxbits = bits;
            }
        }

        private int lowZeros(long value) {
            int i = 1;
            for (; i < 32; i++) {
                if (value >> i << i != value) {
                    break;
                }
            }
            return i - 1;
        }
    }

    /** 管理1个BitKeeper, 输出k和n的规律 */
    static class Experiment {
        private int n;
        private BitKeeper keeper;

        public Experiment(int n) {
            this.n = n;
            this.keeper = new BitKeeper();
        }

        public void work() {
            for (int i = 0; i < n; i++) {
                long m = ThreadLocalRandom.current().nextLong(1L << 32);
                this.keeper.random(m);
            }
        }

        public void debug() {
            System.out.printf("%d %.2f %d\n", this.n, Math.log(this.n) / Math.log(2), this.keeper.maxbits);
        }
    }

    /** 管理1024个BitKeeper, 使用均分 */
    static class ExperimentMultiKeeper {
        private int n;
        private int k;
        private BitKeeper[] keepers;

        public ExperimentMultiKeeper(int n, int k) {
            this.n = n;
            this.k = k;
            this.keepers = new BitKeeper[k];
            for (int i = 0; i < k; i++) {
                this.keepers[i] = new BitKeeper();
            }
        }

        /** 生成 N 个随机数, 然后把随机数hash分散到 bitKeeper 数组里的 bitKeeper 记录起来. */
        public void work() {
            for (int i = 0; i < this.n; i++) {
                long m = ThreadLocalRandom.current().nextLong(1L << 32);
                BitKeeper keeper = keepers[(int) (((m & 0xfff0000) >> 16) % keepers.length)];
                keeper.random(m);
            }
        }

        /** 统计 bitKepper 数组的倒数的平均(倒数平均, 然后取倒数, 更均匀一些), 然后计算规律 log2(K_avg) */
        public double estimate() {
            double sumbitsInverse = 0.0;
            for (BitKeeper keeper : keepers) {
                sumbitsInverse += 1.0 / (float) keeper.maxbits;
            }
            double avgBits = (float) keepers.length / sumbitsInverse;
            return Math.pow(2, avgBits) * this.k;
        }
    }

    /**
     * 使用 bitKeep 记录总数n和最长右0个数k, 发现 `k = log2(n)` 规律.
     */
    public static void main(String[] args) {
        /* 管理1个BitKeeper, 输出k和n的规律, 10000次 */
        for (int i = 1000; i < 100000; i += 100) {
            Experiment exp = new Experiment(i);
            exp.work();
            exp.debug();
        }

        /* 管理1024个BitKeeper, 把随机数均摊, 然后统计K的调和平均(倒数平均), 看K和N的关系, 10000次 */
        for (int i = 100000; i < 1000000; i += 100000) {
            ExperimentMultiKeeper exp = new ExperimentMultiKeeper(i, 1024);
            /* 生成 N(也就是i) 个随机数, 然后把随机数hash分散到 bitKeeper 数组里的 bitKeeper 记录起来. */
            exp.work();
            /* 统计 bitKepper 数组的倒数的平均(倒数平均, 然后取倒数, 更均匀一些), 然后 log2(K_avg) 和 N(i)的规律. */
            double est = exp.estimate();
            System.out.printf("%d %.2f %.2f\n", i, est, Math.abs(est - i) / i);
        }
    }
}

```