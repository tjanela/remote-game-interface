package com.blissapplications.java;

public class ByteArrayUtils {

	public static void dumpHex(StringBuffer sb, byte[] b) {
		for (int i = 0; i < b.length; ++i) {
			if (i % 16 == 0) {
				sb.append(Integer.toHexString((i & 0xFFFF) | 0x10000)
						.substring(1, 5) + " - ");
			}
			sb.append(Integer.toHexString((b[i] & 0xFF) | 0x100)
					.substring(1, 3) + " ");
			if (i % 16 == 15 || i == b.length - 1) {
				int j;
				for (j = 16 - i % 16; j > 1; --j)
					sb.append("   ");
				sb.append(" - ");
				int start = (i / 16) * 16;
				int end = (b.length < i + 1) ? b.length : (i + 1);
				for (j = start; j < end; ++j)
					if (b[j] >= 32 && b[j] <= 126)
						sb.append((char) b[j]);
					else
						sb.append(".");
			}
		}
	}
}
