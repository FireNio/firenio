/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firenio.baseio.protocol;

public class Calculagraph {

    private long alphaTimestamp;
    //	private Logger	logger		= LoggerFactory.getLogger(Calculagraph.class);
    private long currentMark;
    private int  markInterval = 1;
    private long nextMark;
    private int  sequenceNO;

    public Calculagraph(int markInterval) {
        this(markInterval, System.currentTimeMillis());
    }

    public Calculagraph(int markInterval, long currentMark) {
        this.markInterval = markInterval;
        this.currentMark = currentMark;
        this.nextMark = currentMark + markInterval;
        this.alphaTimestamp = currentMark;
        //		logger.debug("________________lastMark______create:{}", currentMark);
    }

    public long getAlphaTimestamp() {
        return alphaTimestamp;
    }

    public int getSequenceNO() {
        return sequenceNO++;
    }

    public long getTimestamp() {

        long current = System.currentTimeMillis();

        if (current < nextMark) {

            return currentMark;
        }

        for (; current >= nextMark;) {

            //			logger.debug("________________current - nextMark:{}", current - nextMark);
            nextMark = nextMark + markInterval;
        }

        currentMark = nextMark - markInterval;

        sequenceNO = 0;

        return currentMark;

    }
}
