package com.tapc.test.interfaces;

import com.tapc.test.utils.SysUtils;

public class CopyListenerInterface {

	public void progress(long index) {
		SysUtils.COPY_FILE_SIZE = index;
	}
}
