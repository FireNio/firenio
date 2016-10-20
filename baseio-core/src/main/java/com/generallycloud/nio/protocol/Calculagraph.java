package com.generallycloud.nio.protocol;

import com.generallycloud.nio.common.ThreadUtil;

public class Calculagraph {

	private int	markInterval	= 1;
	private long	nextMark		;
	private long	currentMark	;
	private int	sequenceNO	;
	private long	alphaTimestamp	;
//	private Logger	logger		= LoggerFactory.getLogger(Calculagraph.class);

	public Calculagraph(int markInterval, long currentMark) {
		this.markInterval = markInterval;
		this.currentMark = currentMark;
		this.nextMark = currentMark + markInterval;
		this.alphaTimestamp = currentMark;
//		logger.debug("________________lastMark______create:{}", currentMark);
	}

	public Calculagraph(int markInterval) {
		this(markInterval, System.currentTimeMillis());
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

	public int getSequenceNO() {
		return sequenceNO++;
	}

	public long getAlphaTimestamp() {
		return alphaTimestamp;
	}

	public static void main(String[] args) {

		Calculagraph calculagraph = new Calculagraph(1000);

		for (;;) {

			long t = calculagraph.getTimestamp();

			int s = calculagraph.getSequenceNO();

			System.out.println("T:" + t + ",S:" + s);

			ThreadUtil.sleep(200);
		}

	}

}
