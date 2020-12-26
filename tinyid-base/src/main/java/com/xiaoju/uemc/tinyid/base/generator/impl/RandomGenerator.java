package com.xiaoju.uemc.tinyid.base.generator.impl;

/**
 * @ClassName: RandomGenerator
 * @Description: todo
 * @Company: xxxxx
 * @Author: zhengcq
 * @Date: 2020/12/26
 */
public class RandomGenerator {

    private long curTime;

    private long randomNum;

    private long  factor;

    private long  totalCount;

    private long  randomDelta;

    public RandomGenerator() {
      randomDelta = 10L;
      reset();
    }

    public RandomGenerator(long randomDelta) {
        this.randomDelta = randomDelta;
        reset();
    }

    public void reset() {
        curTime = System.currentTimeMillis();
        randomNum = (curTime << 20) ^ ((curTime >> 5)) ^ Long.MAX_VALUE;
        totalCount = 0L;
        totalCount += randomNum % 10;
        randomNum = randomNum / 10;
        factor = totalCount % randomDelta;
    }

    public long getFactor() {
        long rs = factor;
        if (randomNum <= 0) {
            reset();
        }
        totalCount += randomNum % 10;
        randomNum = randomNum / 10L;
        factor = totalCount % randomDelta;
        return rs;
    }

    public long getPositiveFactor() {
        long tmp = getFactor();
        if (tmp <= 0L) {
            tmp = getFactor();
            if (tmp <= 0L) {
                totalCount += 7L;
                factor = totalCount % randomDelta;
                tmp = getFactor();
            }
        }
        return tmp;
    }
}
