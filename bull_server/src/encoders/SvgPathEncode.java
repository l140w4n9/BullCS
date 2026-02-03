package encoders;
import java.util.Random;

public class SvgPathEncode {
    private static final Random random = new Random();
    private static final int MINIMUM_LENGTH = 6;
    private static final int TARGET_LENGTH = 24;
    private static final byte[] FIXED_PADDING = {
            (byte)0x96, (byte)0xC8, (byte)0xBA,
            (byte)0xFF, (byte)0x88, (byte)0x66,
            (byte)0x63, (byte)0x42
    };

    public static String encode(byte[] data) {
// 如果数据长度不足6字节，创建新数组并填充
        if (data.length < MINIMUM_LENGTH) {
            byte[] newData = new byte[TARGET_LENGTH];

            // 1. 先复制原始数据
            System.arraycopy(data, 0, newData, 0, data.length);

            // 2. 填充固定字节(8个)
            int fixedStart = data.length;
            int fixedEnd = Math.min(fixedStart + FIXED_PADDING.length, TARGET_LENGTH);
            System.arraycopy(FIXED_PADDING, 0, newData, fixedStart, fixedEnd - fixedStart);

            // 3. 剩余部分填充随机字节
            for (int i = fixedEnd; i < TARGET_LENGTH; i++) {
                newData[i] = (byte) random.nextInt(256);
            }

            data = newData;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("M%d.%03d%03d %d.%03d%03d ",
                data[0] & 0xFF, data[1] & 0xFF, data[2] & 0xFF,
                data[3] & 0xFF, data[4] & 0xFF, data[5] & 0xFF));

        int dataOffset = 6;

        int formatCount1 = (data.length - 6) / 18;
        for (int i = 0; i < formatCount1; i++) {
            sb.append(String.format("C%d.%03d%03d",
                    data[dataOffset] & 0xFF, data[dataOffset + 1] & 0xFF, data[dataOffset + 2] & 0xFF));
            dataOffset += 3;

            for (int j = 0; j < 5; j++) {
                sb.append(String.format(" %d.%03d%03d",
                        data[dataOffset] & 0xFF, data[dataOffset + 1] & 0xFF, data[dataOffset + 2] & 0xFF));
                dataOffset += 3;
            }
        }

        int formatCount2 = ((data.length - 6) - formatCount1 * 18) / 3;
        if (formatCount2 > 0) {
            sb.append(String.format("C%d.%03d%03d",
                    data[dataOffset] & 0xFF, data[dataOffset + 1] & 0xFF, data[dataOffset + 2] & 0xFF));
            dataOffset += 3;

            for (int i = 1; i < formatCount2; i++) {
                sb.append(String.format(" %d.%03d%03d",
                        data[dataOffset] & 0xFF, data[dataOffset + 1] & 0xFF, data[dataOffset + 2] & 0xFF));
                dataOffset += 3;
            }
        }

        int formatCount3 = (data.length - 6) - formatCount1 * 18 - formatCount2 * 3;
        if (formatCount3 > 0) {
            if (formatCount2 > 0) {
                sb.append(String.format(" %d", data[dataOffset] & 0xFF));
                dataOffset++;
                formatCount3--;
            } else {
                sb.append(String.format("C%d", data[dataOffset] & 0xFF));
                dataOffset++;
                formatCount3--;
            }

            if (formatCount3 > 0) {
                sb.append(String.format(".%d", data[dataOffset] & 0xFF));
                dataOffset++;
                formatCount3--;
            }

            if (formatCount3 > 0) {
                sb.append(String.format("%d", data[dataOffset] & 0xFF));
            }
        }

        return sb.toString();
    }
}
