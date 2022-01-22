```java
/**
 * ����һϵ�е�������������Ǽ�¼�µ�λ������λ����󳤶� k��
 * ͨ����� k ֵ���Թ���������������
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

    /** ����1��BitKeeper, ���k��n�Ĺ��� */
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

    /** ����1024��BitKeeper, ʹ�þ��� */
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

        /** ���� N �������, Ȼ��������hash��ɢ�� bitKeeper ������� bitKeeper ��¼����. */
        public void work() {
            for (int i = 0; i < this.n; i++) {
                long m = ThreadLocalRandom.current().nextLong(1L << 32);
                BitKeeper keeper = keepers[(int) (((m & 0xfff0000) >> 16) % keepers.length)];
                keeper.random(m);
            }
        }

        /** ͳ�� bitKepper ����ĵ�����ƽ��(����ƽ��, Ȼ��ȡ����, ������һЩ), Ȼ�������� log2(K_avg) */
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
     * ʹ�� bitKeep ��¼����n�����0����k, ���� `k = log2(n)` ����.
     */
    public static void main(String[] args) {
        /* ����1��BitKeeper, ���k��n�Ĺ���, 10000�� */
        for (int i = 1000; i < 100000; i += 100) {
            Experiment exp = new Experiment(i);
            exp.work();
            exp.debug();
        }

        /* ����1024��BitKeeper, ���������̯, Ȼ��ͳ��K�ĵ���ƽ��(����ƽ��), ��K��N�Ĺ�ϵ, 10000�� */
        for (int i = 100000; i < 1000000; i += 100000) {
            ExperimentMultiKeeper exp = new ExperimentMultiKeeper(i, 1024);
            /* ���� N(Ҳ����i) �������, Ȼ��������hash��ɢ�� bitKeeper ������� bitKeeper ��¼����. */
            exp.work();
            /* ͳ�� bitKepper ����ĵ�����ƽ��(����ƽ��, Ȼ��ȡ����, ������һЩ), Ȼ�� log2(K_avg) �� N(i)�Ĺ���. */
            double est = exp.estimate();
            System.out.printf("%d %.2f %.2f\n", i, est, Math.abs(est - i) / i);
        }
    }
}

```