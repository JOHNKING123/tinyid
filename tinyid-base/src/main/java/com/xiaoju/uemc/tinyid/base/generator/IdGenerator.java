package com.xiaoju.uemc.tinyid.base.generator;

import java.util.List;

/**
 * @author du_imba
 */
public interface IdGenerator {
    /**
     * get next id
     * @return
     */
    Long nextId();


    /**
     * get next id
     * @return
     */
    Long nextIdWithDelta(long delta);

    /**
     * get next id batch
     * @param batchSize
     * @return
     */
    List<Long> nextId(Integer batchSize);

    /**
     * get next id batch
     * @param batchSize
     * @return
     */
    List<Long> nextIdWithRandom(Integer batchSize);
}
